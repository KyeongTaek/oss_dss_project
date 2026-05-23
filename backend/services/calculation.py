from typing import Any, Optional, Dict


# =========================
# Constants
# =========================

HEATING_STRONGLY_RECOMMENDED = "HEATING_STRONGLY_RECOMMENDED"
HEATING_RECOMMENDED = "HEATING_RECOMMENDED"
HEATING_NOT_RECOMMENDED = "HEATING_NOT_RECOMMENDED"

COOLING_STRONGLY_RECOMMENDED = "COOLING_STRONGLY_RECOMMENDED"
COOLING_RECOMMENDED = "COOLING_RECOMMENDED"
COOLING_NOT_RECOMMENDED = "COOLING_NOT_RECOMMENDED"

VENTILATION_STRONGLY_RECOMMENDED = "VENTILATION_STRONGLY_RECOMMENDED"
VENTILATION_RECOMMENDED = "VENTILATION_RECOMMENDED"
VENTILATION_NOT_RECOMMENDED = "VENTILATION_NOT_RECOMMENDED"

OPERATING_COOLING_REQUIRED = "COOLING_REQUIRED"
OPERATING_HEATING_REQUIRED = "HEATING_REQUIRED"
OPERATING_POWER_SAVING = "POWER_SAVING"


# =========================
# Basic validation utilities
# =========================

def to_float_or_none(value: Any) -> Optional[float]:
    """
    입력값을 float으로 변환한다.
    None, 문자열 오류, bool 값 등 계산에 부적절한 값은 None으로 처리한다.
    """
    if value is None:
        return None

    if isinstance(value, bool):
        return None

    try:
        return float(value)
    except (TypeError, ValueError):
        return None


def validate_range(
    value: Any,
    min_value: float,
    max_value: float,
) -> Optional[float]:
    """
    값이 정상 범위 안에 있으면 float으로 반환하고,
    값이 없거나 정상 범위를 벗어나면 None을 반환한다.
    """
    numeric_value = to_float_or_none(value)

    if numeric_value is None:
        return None

    if numeric_value < min_value or numeric_value > max_value:
        return None

    return numeric_value


def validate_temperature(value: Any) -> Optional[float]:
    """
    기온 정상 범위: -40 ~ 80
    """
    return validate_range(value, -40, 80)


def validate_humidity(value: Any) -> Optional[float]:
    """
    습도 정상 범위: 0 ~ 100
    """
    return validate_range(value, 0, 100)


def validate_co2(value: Any) -> Optional[float]:
    """
    CO2 정상 범위: 400 ~ 2000
    """
    return validate_range(value, 400, 2000)


def validate_aqi(value: Any) -> Optional[float]:
    """
    AQI 정상 범위: 1 ~ 5
    """
    return validate_range(value, 1, 5)


def sanitize_environment_data(
    temperature: Any = None,
    humidity: Any = None,
    co2: Any = None,
    aqi: Any = None,
) -> Dict[str, Optional[float]]:
    """
    센서 데이터의 아웃라이어를 검증한다.
    정상 범위를 벗어나거나 값이 없으면 None으로 변환한다.

    실제 DB 적재 단계에서는 None이 된 값은 저장하지 않거나,
    이후 계산에서 None 전파 대상으로 처리하면 된다.
    """
    return {
        "temperature": validate_temperature(temperature),
        "humidity": validate_humidity(humidity),
        "co2": validate_co2(co2),
        "aqi": validate_aqi(aqi),
    }


# =========================
# None propagation utilities
# =========================

def has_none(*values: Any) -> bool:
    """
    전달된 값 중 하나라도 None이면 True를 반환한다.
    """
    return any(value is None for value in values)


def return_none_if_missing(calculated_value: Any, *dependencies: Any) -> Any:
    """
    특정 계산값이 여러 입력값에 의존할 때,
    입력값 중 하나라도 None이면 계산 결과도 None으로 처리한다.
    """
    if has_none(*dependencies):
        return None

    return calculated_value


# =========================
# Heating need calculation
# =========================

def calculate_heating_need(external_temperature: Any) -> Optional[str]:
    """
    건물 외부 온도를 기준으로 난방 필요도를 계산한다.

    기준:
    - 8도 이하: 난방 강력 추천
    - 10도 이하: 난방 추천
    - 10도 초과: 난방 비추천

    입력값이 None이거나 아웃라이어이면 None을 반환한다.
    """
    temperature = validate_temperature(external_temperature)

    if temperature is None:
        return None

    if temperature <= 8:
        return HEATING_STRONGLY_RECOMMENDED

    if temperature <= 10:
        return HEATING_RECOMMENDED

    return HEATING_NOT_RECOMMENDED


# =========================
# Final operation status
# =========================

def determine_operating_status(
    temperature_delta: Any,
    ventilation_suitability: Any,
    cooling_need: Any,
    heating_need: Any,
) -> Optional[str]:
    """
    4가지 연산 지표를 종합하여 최종 운영 상태를 결정한다.

    4가지 지표 중 하나라도 None이면 최종 운영 상태도 None이다.

    운영 상태 기준:
    - 냉방 필요도 강력 추천/추천: COOLING_REQUIRED
    - 난방 필요도 강력 추천/추천: HEATING_REQUIRED
    - 냉방, 난방 모두 비추천: POWER_SAVING
    """
    if has_none(
        temperature_delta,
        ventilation_suitability,
        cooling_need,
        heating_need,
    ):
        return None

    if cooling_need in {
        COOLING_STRONGLY_RECOMMENDED,
        COOLING_RECOMMENDED,
    }:
        return OPERATING_COOLING_REQUIRED

    if heating_need in {
        HEATING_STRONGLY_RECOMMENDED,
        HEATING_RECOMMENDED,
    }:
        return OPERATING_HEATING_REQUIRED

    return OPERATING_POWER_SAVING


def build_operation_result(
    temperature_delta: Any,
    ventilation_suitability: Any,
    cooling_need: Any,
    heating_need: Any,
    recommendation_msg: Optional[str],
) -> Dict[str, Optional[str]]:
    """
    최종 운영 상태와 추천 메시지를 만든다.

    4가지 연산 지표 중 하나라도 None이면
    operating_status와 recommendation_msg를 모두 None으로 처리한다.
    """
    operating_status = determine_operating_status(
        temperature_delta=temperature_delta,
        ventilation_suitability=ventilation_suitability,
        cooling_need=cooling_need,
        heating_need=heating_need,
    )

    if operating_status is None:
        return {
            "operating_status": None,
            "recommendation_msg": None,
        }

    return {
        "operating_status": operating_status,
        "recommendation_msg": recommendation_msg,
    }


# =========================
# Unit debugging
# =========================

def run_unit_debugging() -> None:
    """
    12주차 단위 디버깅용 함수.
    null 또는 100도 같은 극단값을 넣어 None 전파 여부를 확인한다.
    """

    # Temperature validation
    assert validate_temperature(None) is None
    assert validate_temperature(100) is None
    assert validate_temperature(-41) is None
    assert validate_temperature(-40) == -40.0
    assert validate_temperature(80) == 80.0
    assert validate_temperature(25) == 25.0

    # Humidity validation
    assert validate_humidity(None) is None
    assert validate_humidity(-1) is None
    assert validate_humidity(101) is None
    assert validate_humidity(44.5) == 44.5

    # CO2 validation
    assert validate_co2(None) is None
    assert validate_co2(399) is None
    assert validate_co2(2001) is None
    assert validate_co2(480) == 480.0

    # AQI validation
    assert validate_aqi(None) is None
    assert validate_aqi(0) is None
    assert validate_aqi(6) is None
    assert validate_aqi(2) == 2.0

    # Heating need calculation
    assert calculate_heating_need(None) is None
    assert calculate_heating_need(100) is None
    assert calculate_heating_need(8) == HEATING_STRONGLY_RECOMMENDED
    assert calculate_heating_need(9) == HEATING_RECOMMENDED
    assert calculate_heating_need(10) == HEATING_RECOMMENDED
    assert calculate_heating_need(11) == HEATING_NOT_RECOMMENDED

    # None propagation
    assert determine_operating_status(
        temperature_delta=None,
        ventilation_suitability=VENTILATION_RECOMMENDED,
        cooling_need=COOLING_NOT_RECOMMENDED,
        heating_need=HEATING_NOT_RECOMMENDED,
    ) is None

    assert build_operation_result(
        temperature_delta=None,
        ventilation_suitability=VENTILATION_RECOMMENDED,
        cooling_need=COOLING_NOT_RECOMMENDED,
        heating_need=HEATING_NOT_RECOMMENDED,
        recommendation_msg="테스트 메시지",
    ) == {
        "operating_status": None,
        "recommendation_msg": None,
    }

    assert determine_operating_status(
        temperature_delta=0.1,
        ventilation_suitability=VENTILATION_RECOMMENDED,
        cooling_need=COOLING_RECOMMENDED,
        heating_need=HEATING_NOT_RECOMMENDED,
    ) == OPERATING_COOLING_REQUIRED

    assert determine_operating_status(
        temperature_delta=0.1,
        ventilation_suitability=VENTILATION_RECOMMENDED,
        cooling_need=COOLING_NOT_RECOMMENDED,
        heating_need=HEATING_RECOMMENDED,
    ) == OPERATING_HEATING_REQUIRED

    assert determine_operating_status(
        temperature_delta=0.1,
        ventilation_suitability=VENTILATION_RECOMMENDED,
        cooling_need=COOLING_NOT_RECOMMENDED,
        heating_need=HEATING_NOT_RECOMMENDED,
    ) == OPERATING_POWER_SAVING

    print("All calculation unit checks passed.")


if __name__ == "__main__":
    run_unit_debugging()