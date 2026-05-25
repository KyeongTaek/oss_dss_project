from services.sensor_sync import sync_latest_sensor_data
import os
from decimal import Decimal
from typing import Any, Optional

import aiomysql
from dotenv import load_dotenv
from fastapi import FastAPI


load_dotenv()

app = FastAPI(
    title="OSS DSS Analysis Server",
    description="Campus operation status API server",
    version="0.2.0",
)


def get_required_env(name: str) -> str:
    value = os.getenv(name)

    if value is None or value.strip() == "":
        raise RuntimeError(f"Missing DB environment variable: {name}")

    return value


def to_json_value(value: Any) -> Any:
    if isinstance(value, Decimal):
        return float(value)

    return value


def first_non_null(rows: list[dict], key: str) -> Optional[Any]:
    for row in rows:
        value = row.get(key)
        if value is not None:
            return to_json_value(value)

    return None


@app.on_event("startup")
async def startup_event():
    app.state.mysql_pool = await aiomysql.create_pool(
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


@app.on_event("shutdown")
async def shutdown_event():
    pool = app.state.mysql_pool
    pool.close()
    await pool.wait_closed()


@app.get("/health")
def health_check():
    return {
        "statusCode": 200,
        "message": "server is running",
        "data": None,
    }


@app.post("/api/campus/refresh")
async def refresh_campus_data():
    result = await sync_latest_sensor_data(app.state.mysql_pool)

    return {
        "statusCode": 200,
        "message": "Data synchronized successfully!",
        "data": result,
    }


@app.get("/api/campus/status")
async def get_campus_status():
    query = """
        SELECT
            bs.building_name,
            bs.ext_temp,
            bs.ext_co2,
            bs.`Avg_humidity` AS campus_humidity,
            bs.avg_aqi,
            bs.operation_status,
            r.operation_msg AS recommendation_msg
        FROM building_status bs
        LEFT JOIN operation_rules r
            ON bs.rule_code = r.rule_code
        ORDER BY bs.id;
    """

    async with app.state.mysql_pool.acquire() as connection:
        async with connection.cursor(aiomysql.DictCursor) as cursor:
            await cursor.execute(query)
            rows = await cursor.fetchall()

    buildings = []

    for row in rows:
        operating_status = row.get("operation_status")

        buildings.append(
            {
                "building_name": row.get("building_name"),
                "building_ext_temp": to_json_value(row.get("ext_temp")),
                "building_ext_co2": row.get("ext_co2"),
                "operating_status": operating_status,
                "recommendation_msg": (
                    row.get("recommendation_msg")
                    if operating_status is not None
                    else None
                ),
            }
        )

    return {
        "statusCode": 200,
        "message": "request success",
        "data": {
            "campus_humidity": first_non_null(rows, "campus_humidity"),
            "campus_aqi": first_non_null(rows, "avg_aqi"),
            "buildings": buildings,
        },
    }

