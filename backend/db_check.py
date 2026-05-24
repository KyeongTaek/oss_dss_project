import asyncio
import os

from dotenv import load_dotenv
from tortoise import Tortoise, connections


load_dotenv()


def build_db_url() -> str:
    db_host = os.getenv("DB_HOST")
    db_port = os.getenv("DB_PORT", "3306")
    db_user = os.getenv("DB_USER")
    db_password = os.getenv("DB_PASSWORD")
    db_name = os.getenv("DB_NAME")

    missing = [
        name
        for name, value in {
            "DB_HOST": db_host,
            "DB_USER": db_user,
            "DB_PASSWORD": db_password,
            "DB_NAME": db_name,
        }.items()
        if not value
    ]

    if missing:
        raise RuntimeError(f"Missing DB environment variables: {', '.join(missing)}")

    return f"mysql://{db_user}:{db_password}@{db_host}:{db_port}/{db_name}"


async def check_mysql_connection() -> None:
    db_url = build_db_url()

    await Tortoise.init(
        db_url=db_url,
        modules={"models": []},
    )

    connection = connections.get("default")
    result = await connection.execute_query("SELECT 1")

    await Tortoise.close_connections()

    print("MySQL connection pool check passed.")
    print(result)


if __name__ == "__main__":
    asyncio.run(check_mysql_connection())
