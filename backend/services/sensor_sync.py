import os
from typing import Any

import aiomysql
import httpx

from services.calculation import (
    validate_temperature,
    validate_humidity,
    validate_aqi,
    validate_co2,
)


def get_env_url(name: str) -> str:
    value = os.getenv(name)

    if value is None or value.strip() == "":
        raise RuntimeError(f"Missing environment variable: {name}")

    return value


def get_value(data: dict, *keys: str) -> Any:
    for key in keys:
        if key in data:
            return data.get(key)
    return None


def normalize_regular_sensor(raw: dict) -> dict | None:
    sensor = raw.get("sensor")
    temp = validate_temperature(raw.get("temperature"))
    eco2 = validate_co2(raw.get("co2"))

    if not sensor:
        return None

    if temp is None or eco2 is None:
        return None

    return {
        "sensor": sensor,
        "temp": temp,
        "humidity": None,
        "aqi": None,
        "eco2": int(eco2),
    }


def normalize_special_sensor(raw: dict) -> dict | None:
    sensor = raw.get("sensor")
    temp = validate_temperature(get_value(raw, "temp", "temperature"))
    humidity = validate_humidity(raw.get("humidity"))
    aqi = validate_aqi(get_value(raw, "aqi", "AQI"))
    eco2 = validate_co2(get_value(raw, "eco2", "eCO2", "co2"))

    if not sensor:
        return None

    if temp is None or humidity is None or aqi is None or eco2 is None:
        return None

    return {
        "sensor": sensor,
        "temp": temp,
        "humidity": humidity,
        "aqi": int(aqi),
        "eco2": int(eco2),
    }


async def fetch_json_array(url: str) -> list[dict]:
    async with httpx.AsyncClient(timeout=10.0, follow_redirects=True) as client:
        response = await client.get(url)
        response.raise_for_status()
        data = response.json()

    if not isinstance(data, list):
        raise RuntimeError(f"Expected JSON array from {url}")

    return data


async def insert_sensor_rows(pool: aiomysql.Pool, rows: list[dict]) -> int:
    if not rows:
        return 0

    query = """
        INSERT INTO sensor_data
            (sensor, temp, humidity, aqi, eco2)
        VALUES
            (%s, %s, %s, %s, %s);
    """

    values = [
        (
            row["sensor"],
            row["temp"],
            row["humidity"],
            row["aqi"],
            row["eco2"],
        )
        for row in rows
    ]

    async with pool.acquire() as connection:
        async with connection.cursor() as cursor:
            await cursor.executemany(query, values)

    return len(values)


async def sync_latest_sensor_data(pool: aiomysql.Pool) -> dict:
    map_url = get_env_url("CLASS_SERVER_MAP_URL")
    open_src_url = get_env_url("CLASS_SERVER_OPEN_SRC_URL")

    regular_raw = await fetch_json_array(map_url)
    special_raw = await fetch_json_array(open_src_url)

    normalized_rows: list[dict] = []
    skipped_count = 0

    for item in regular_raw:
        row = normalize_regular_sensor(item)
        if row is None:
            skipped_count += 1
        else:
            normalized_rows.append(row)

    for item in special_raw:
        row = normalize_special_sensor(item)
        if row is None:
            skipped_count += 1
        else:
            normalized_rows.append(row)

    inserted_count = await insert_sensor_rows(pool, normalized_rows)

    return {
        "regular_received": len(regular_raw),
        "special_received": len(special_raw),
        "inserted_count": inserted_count,
        "skipped_count": skipped_count,
    }
