import os
from decimal import Decimal
from typing import Any

import aiomysql
from dotenv import load_dotenv

from services.sensor_sync import sync_latest_sensor_data

from services.calculation import (
    calculate_discomfort_index,
    calculate_cooling_need,
    calculate_ventilation_suitability,
    calculate_heating_need,
    calculate_temperature_change,
)

load_dotenv()


def get_required_env(name: str) -> str:
    value = os.getenv(name)

    if value is None or value.strip() == "":
        raise RuntimeError(f"Missing environment variable: {name}")

    return value


def to_float_or_none(value: Any) -> float | None:
    if value is None:
        return None

    if isinstance(value, Decimal):
        return float(value)

    try:
        return float(value)
    except (TypeError, ValueError):
        return None


def average_if_all_present(values: list[Any]) -> float | None:
    """
    건물별 센서 3개 중 하나라도 값이 없으면 None 처리한다.
    요구사항의 데이터 누락 시 null 전파 정책에 맞춘다.
    """
    numeric_values = [to_float_or_none(value) for value in values]

    if any(value is None for value in numeric_values):
        return None

    return round(sum(numeric_values) / len(numeric_values), 2)


async def fetch_latest_sensor_rows(pool: aiomysql.Pool) -> dict[str, dict]:
    """
    sensor_data에서 센서별 최신 데이터 1개씩 가져온다.
    최신 기준은 id가 가장 큰 row.
    """
    query = """
        SELECT
            sd.sensor,
            sd.temp,
            sd.humidity,
            sd.aqi,
            sd.eco2
        FROM sensor_data sd
        INNER JOIN (
            SELECT sensor, MAX(id) AS max_id
            FROM sensor_data
            GROUP BY sensor
        ) latest
            ON sd.sensor = latest.sensor
            AND sd.id = latest.max_id;
    """

    async with pool.acquire() as connection:
        async with connection.cursor(aiomysql.DictCursor) as cursor:
            await cursor.execute(query)
            rows = await cursor.fetchall()

    return {row["sensor"]: row for row in rows}

async def fetch_past_sensor_rows(pool: aiomysql.Pool) -> dict[str, dict]:
    """
    sensor_data에서 센서별 5~15분 전 데이터 중 가장 최신 데이터 1개씩 가져온다.
    temp_change 계산용이다.
    """
    query = """
        SELECT
            sd.sensor,
            sd.temp,
            sd.humidity,
            sd.aqi,
            sd.eco2
        FROM sensor_data sd
        INNER JOIN (
            SELECT sensor, MAX(id) AS max_id
            FROM sensor_data
            WHERE created_at BETWEEN DATE_SUB(NOW(), INTERVAL 15 MINUTE)
                                AND DATE_SUB(NOW(), INTERVAL 5 MINUTE)
            GROUP BY sensor
        ) past
            ON sd.sensor = past.sensor
            AND sd.id = past.max_id;
    """

    async with pool.acquire() as connection:
        async with connection.cursor(aiomysql.DictCursor) as cursor:
            await cursor.execute(query)
            rows = await cursor.fetchall()

    return {row["sensor"]: row for row in rows}


async def fetch_building_sensor_map(pool: aiomysql.Pool) -> list[dict]:
    query = """
        SELECT
            building_name,
            sensor_name1,
            sensor_name2,
            sensor_name3
        FROM building_sensor_map
        ORDER BY building_name;
    """

    async with pool.acquire() as connection:
        async with connection.cursor(aiomysql.DictCursor) as cursor:
            await cursor.execute(query)
            rows = await cursor.fetchall()

    return list(rows)


async def calculate_campus_environment(pool: aiomysql.Pool) -> dict:
    """
    특수 센서 최신값 기준으로 캠퍼스 평균 습도/AQI를 계산한다.
    현재 sensor 이름이 opensrc_team 형태라서 해당 데이터만 사용한다.
    """
    query = """
        SELECT
            AVG(latest_data.humidity) AS campus_humidity,
            AVG(latest_data.aqi) AS campus_aqi
        FROM (
            SELECT sd.*
            FROM sensor_data sd
            INNER JOIN (
                SELECT sensor, MAX(id) AS max_id
                FROM sensor_data
                GROUP BY sensor
            ) latest
                ON sd.sensor = latest.sensor
                AND sd.id = latest.max_id
        ) latest_data
        WHERE latest_data.sensor LIKE 'opensrc%'
          AND latest_data.humidity IS NOT NULL
          AND latest_data.aqi IS NOT NULL;
    """

    async with pool.acquire() as connection:
        async with connection.cursor(aiomysql.DictCursor) as cursor:
            await cursor.execute(query)
            row = await cursor.fetchone()

    return {
        "campus_humidity": (
            round(float(row["campus_humidity"]), 2)
            if row and row["campus_humidity"] is not None
            else None
        ),
        "campus_aqi": (
            round(float(row["campus_aqi"]), 2)
            if row and row["campus_aqi"] is not None
            else None
        ),
    }


async def remove_invalid_building_status_rows(pool: aiomysql.Pool) -> int:
    """
    building_sensor_map에 없는 테스트용 건물명을 building_status에서 제거한다.
    """
    query = """
        DELETE FROM building_status
        WHERE building_name NOT IN (
            SELECT building_name FROM building_sensor_map
        );
    """

    async with pool.acquire() as connection:
        async with connection.cursor() as cursor:
            deleted_count = await cursor.execute(query)

    return deleted_count


async def upsert_building_environment(
    pool: aiomysql.Pool,
    building_name: str,
    ext_temp: float | None,
    ext_co2: float | None,
    campus_humidity: float | None,
    campus_aqi: float | None,
    discomfort_idx: float | None,
    cooling_need: int | None,
    ventilation: int | None,
    heating_need: int | None,
    temp_change: float | None,
) -> str:
    """
    building_status에 건물이 있으면 UPDATE,
    없으면 INSERT 한다.

    아직 냉방/난방/환기/상태 계산은 하지 않으므로
    ext_temp, ext_co2, Avg_humidity, avg_aqi만 갱신한다.
    """
    async with pool.acquire() as connection:
        async with connection.cursor(aiomysql.DictCursor) as cursor:
            await cursor.execute(
                """
                SELECT id
                FROM building_status
                WHERE building_name = %s
                ORDER BY id
                LIMIT 1;
                """,
                (building_name,),
            )
            existing_row = await cursor.fetchone()

            if existing_row:
                await cursor.execute(
                    """
            UPDATE building_status
		    SET
		        ext_temp = %s,
                ext_co2 = %s,
                Avg_humidity = %s,
                avg_aqi = %s,
                discomfort_idx = %s,
                cooling_need = %s,
                ventilation = %s,
                heating_need = %s,
                temp_change = %s,
                operation_status = NULL,
                rule_code = NULL,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = %s;
                    """,
                    (
    ext_temp,
    ext_co2,
    campus_humidity,
    campus_aqi,
    discomfort_idx,
    cooling_need,
    ventilation,
    heating_need,
    temp_change,
    existing_row["id"],
),
                )
                return "updated"

            await cursor.execute(
                """
                INSERT INTO building_status
    (
        building_name,
        ext_temp,
        ext_co2,
        Avg_humidity,
        avg_aqi,
        discomfort_idx,
        cooling_need,
        ventilation,
        heating_need,
        temp_change
    )
VALUES
    (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s);
                """,
                (
                    building_name,
                    ext_temp,
                    ext_co2,
                    campus_humidity,
                    campus_aqi,
                    discomfort_idx,
                    cooling_need,
                    ventilation,
                    heating_need,
                    temp_change,
                ),
            )
            return "inserted"


async def update_building_environment(pool: aiomysql.Pool) -> dict:
    """
    sensor_data와 building_sensor_map을 기반으로
    building_status의 건물별 외부 온도/CO2, 캠퍼스 평균 습도/AQI를 갱신한다.
    """
    deleted_count = await remove_invalid_building_status_rows(pool)

    latest_sensors = await fetch_latest_sensor_rows(pool)
    building_maps = await fetch_building_sensor_map(pool)
    past_sensors = await fetch_past_sensor_rows(pool)
    campus_environment = await calculate_campus_environment(pool)
    campus_humidity = campus_environment["campus_humidity"]
    campus_aqi = campus_environment["campus_aqi"]
    campus_humidity = campus_environment["campus_humidity"]
    campus_aqi = campus_environment["campus_aqi"]

    updated_count = 0
    inserted_count = 0
    null_environment_count = 0

    for building in building_maps:
        building_name = building["building_name"]

        sensor_names = [
            building["sensor_name1"],
            building["sensor_name2"],
            building["sensor_name3"],
        ]

        sensor_rows = [
            latest_sensors.get(sensor_name)
            for sensor_name in sensor_names
        ]

        temps = [
            row.get("temp") if row else None
            for row in sensor_rows
        ]

        eco2_values = [
            row.get("eco2") if row else None
            for row in sensor_rows
        ]

        ext_temp = average_if_all_present(temps)
        ext_co2 = average_if_all_present(eco2_values)

        past_sensor_rows = [
            past_sensors.get(sensor_name)
            for sensor_name in sensor_names
        ]

        past_temps = [
            row.get("temp") if row else None
            for row in past_sensor_rows
        ]

        past_ext_temp = average_if_all_present(past_temps)
        temp_change = calculate_temperature_change(ext_temp, past_ext_temp)

        discomfort_idx = calculate_discomfort_index(ext_temp, campus_humidity)
        cooling_need = calculate_cooling_need(ext_temp, campus_humidity)
        ventilation = calculate_ventilation_suitability(ext_co2, campus_aqi)
        heating_need = calculate_heating_need(ext_temp)

        if ext_temp is None or ext_co2 is None:
            null_environment_count += 1

        result = await upsert_building_environment(
        pool=pool,
        building_name=building_name,
        ext_temp=ext_temp,
        ext_co2=ext_co2,
        campus_humidity=campus_humidity,
        campus_aqi=campus_aqi,
        discomfort_idx=discomfort_idx,
        cooling_need=cooling_need,
        ventilation=ventilation,
        heating_need=heating_need,
        temp_change=temp_change,
    )

        if result == "updated":
            updated_count += 1
        else:
            inserted_count += 1

    return {
        "deleted_invalid_rows": deleted_count,
        "total_buildings": len(building_maps),
        "updated_count": updated_count,
        "inserted_count": inserted_count,
        "null_environment_count": null_environment_count,
        "campus_humidity": campus_environment["campus_humidity"],
        "campus_aqi": campus_environment["campus_aqi"],
        "cooling_need_calculated": True,
        "ventilation_calculated": True,
        "temperature_change_calculated": True,
    }


async def create_mysql_pool() -> aiomysql.Pool:
    return await aiomysql.create_pool(
        host=get_required_env("DB_HOST"),
        port=int(os.getenv("DB_PORT", "3306")),
        user=get_required_env("DB_USER"),
        password=get_required_env("DB_PASSWORD"),
        db=get_required_env("DB_NAME"),
        charset="utf8mb4",
        autocommit=True,
        minsize=1,
        maxsize=10,
    )


async def main() -> None:
    pool = await create_mysql_pool()

    try:
        result = await update_building_environment(pool)
        print(result)
    finally:
        pool.close()
        await pool.wait_closed()


if __name__ == "__main__":
    import asyncio

    asyncio.run(main())
