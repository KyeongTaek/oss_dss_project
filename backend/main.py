from fastapi import FastAPI

app = FastAPI(
    title="OSS DSS Analysis Server",
    description="Mock API server for campus operation status",
    version="0.1.0",
)


@app.get("/health")
def health_check():
    return {
        "statusCode": 200,
        "message": "server is running",
        "data": None,
    }


@app.post("/api/campus/refresh")
def refresh_campus_data():
    return {
        "statusCode": 200,
        "message": "Data received from app successfully!",
        "data": None,
    }


@app.get("/api/campus/status")
def get_campus_status():
    return {
        "statusCode": 200,
        "message": "request success",
        "data": {
            "campus_humidity": 44.5,
            "campus_aqi": 2,
            "buildings": [
                {
                    "building_name": "공과대학 4호관",
                    "building_ext_temp": 26.5,
                    "building_ext_co2": 480,
                    "operating_status": "COOLING_REQUIRED",
                    "recommendation_msg": "환기하기 좋은 날씨네요. 그러나 외기 온도가 매우 가파르게 상승하고 있어, 창문을 열기보다는 냉방을 유지하는 것이 에너지를 절약하는 길입니다.",
                },
                {
                    "building_name": "중앙도서관",
                    "building_ext_temp": 7.8,
                    "building_ext_co2": 650,
                    "operating_status": "HEATING_REQUIRED",
                    "recommendation_msg": "외기 온도가 낮아 난방이 요구됩니다. 외기질은 양호하므로 최소한의 틈새 환기를 병행하며 난방을 운영하세요.",
                },
                {
                    "building_name": "학생회관",
                    "building_ext_temp": 18.3,
                    "building_ext_co2": 690,
                    "operating_status": "POWER_SAVING",
                    "recommendation_msg": "현재 냉난방이 필요 없는 최적의 외부 기후입니다. 적극적인 자연환기를 전개하고, 모든 공조 설비를 절전/송풍 모드로 전환하여 에너지를 절약하세요.",
                },
                {
                    "building_name": "테스트 결측 건물",
                    "building_ext_temp": None,
                    "building_ext_co2": None,
                    "operating_status": None,
                    "recommendation_msg": None,
                },
            ],
        },
    }