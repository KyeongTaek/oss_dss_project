import asyncio
import os

from dotenv import load_dotenv
from tortoise import Tortoise, connections


load_dotenv()


def get_required_env(name: str) -> str:
    value = os.getenv(name)

    if value is None or value.strip() == "":
        raise RuntimeError(f"Missing DB environment variable: {name}")

    return value


def build_tortoise_config() -> dict:
    db_host = get_required_env("DB_HOST")
    db_port = int(os.getenv("DB_PORT", "3306"))
    db_user = get_required_env("DB_USER")
    db_password = get_required_env("DB_PASSWORD")
    db_name = get_required_env("DB_NAME")

    return {
        "connections": {
            "default": {
                "engine": "tortoise.backends.mysql",
                "credentials": {
                    "host": db_host,
                    "port": db_port,
                    "user": db_user,
                    "password": db_password,
                    "database": db_name,
                    "charset": "utf8mb4",
                },
            }
        },
        "apps": {
            "models": {
                "models": ["db_models"],
                "default_connection": "default",
            }
        },
    }


async def check_mysql_connection() -> None:
    config = build_tortoise_config()

    try:
        await Tortoise.init(config=config)

        connection = connections.get("default")

        result = await connection.execute_query("SELECT 1 AS connection_test;")
        tables = await connection.execute_query("SHOW TABLES;")

        print("MySQL connection pool check passed.")
        print("SELECT 1 result:")
        print(result)

        print("\nTables:")
        print(tables)

    finally:
        await Tortoise.close_connections()


if __name__ == "__main__":
    asyncio.run(check_mysql_connection())
