# OSS_DSS_PROJECT

## ✨ 프로젝트 소개
건물별 센서 데이터를 활용한 캠퍼스 냉난방 운영 의사결정 지원 시스템(Decision Support System)

### 개발 기간
2026.03 ~ 2026.06

### 시연
#### 1) 하단 카드 변경
<img width="400" height="225" alt="Image" src="https://github.com/user-attachments/assets/7f12999c-3929-44e8-814f-5a9d5b69c321" />

#### 2) 백그라운드 동작
<img width="400" height="225" alt="Image" src="https://github.com/user-attachments/assets/c15b8170-1e59-4cbd-a68e-737fb2cfaa53" />

#### 3) common sensor 센싱
<img width="400" height="225" alt="Image" src="https://github.com/user-attachments/assets/dee481fe-bb59-48cb-8f40-4218e8287ae3" />

#### 4) opensrc sensor 센싱
<img width="400" height="225" alt="Image" src="https://github.com/user-attachments/assets/7d1dc4a6-f109-444b-a7be-0be1577d2b55" />

<br />

## 💡 프로젝트 배경 및 주요 기능
### 배경
- 냉난방 시, 비효율적 중앙제어방식으로 인한 불필요한 에너지 발생

- 냉난방기를 직접 제어하는 것이 아니라, 관리자와 사용자가 효율적인 냉난방 운영을 판단할 수 있도록 돕는 의사결정 지원 시스템

- 건물별 실시간 환경을 분석하여 데이터 기반으로 건물의 냉난방 및 환기 필요 여부 제안
### 주요 기능
- 데이터 모니터링 및 수치 계산
- 상태 시각화
- 운영 메시지 권고
### 기대 효과
- 데이터 기반 자동 판단
- 사용자 쾌적도 만족
- 불필요한 냉난방 절감

<br />

## 🛠️ Tech Stack ⚙️
| 분야 | 기술 | 선정 이유 |
|------|------| ----- |
| **언어** | ![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white) ![Python](https://img.shields.io/badge/python-3670A0?style=for-the-badge&logo=python&logoColor=ffdd54)| 안드로이드 컴포넌트와의 호환성이 검증된 Java를 채택하고, 빠른 프로토타이핑과 데이터 처리에 강점이 있는 Python을 활용하여 개발 생산성 극대화. |
| **버전관리 & 협업** | ![Git](https://img.shields.io/badge/git-%23F05033.svg?style=for-the-badge&logo=git&logoColor=white) ![GitHub](https://img.shields.io/badge/github-%23121011.svg?style=for-the-badge&logo=github&logoColor=white) | 팀원 간 원활간 소스코드 통합 및 버전 관리를 위한 환경 구축. |
| **앱 UI & 백엔드** | ![Android Studio](https://img.shields.io/badge/Android%20Studio-3DDC84.svg?style=for-the-badge&logo=android-studio&logoColor=white) | 빠른 배포를 위해 Android 플랫폼 채택. |
| **데이터베이스** | ![MySQL](https://img.shields.io/badge/mysql-4479A1.svg?style=for-the-badge&logo=mysql&logoColor=white) | 센서 위치, 건물 정보 등 구조화된 데이터 간의 관계를 명확히 정의하고, 대용량 시계열 데이터 조회를 위한 인덱스 최적화가 용이한 RDBMS인 MySQL 선택. |
| **서버** | ![FastAPI](https://img.shields.io/badge/FastAPI-005571.svg?style=for-the-badge&logo=fastapi) <br> ![Oracle](https://img.shields.io/badge/Oracle-F80000?style=for-the-badge&logo=oracle&logoColor=white) <br> ![Nginx](https://img.shields.io/badge/nginx-%23009639.svg?style=for-the-badge&logo=nginx&logoColor=white) | Python 웹 프레임워크 중에서 가벼운 편이고, 실시간 데이터 트래픽을 비동기 처리를 통해 효율적으로 수용하기 위해 사용. <br> 안정적인 인프라를 무료로 구축하기 위해 Oracle Cloud Free Tier 활용. <br> FastAPI 앞단에서 리버스 프록시 역할을 수행하며, 보안 강화(fail2ban 연동을 통한 브루트포스 공격 방어) 및 무중단 서버 환경 구축을 위해 채택. |

## 🔍 아키텍처 개요
<img width="1658" height="1253" alt="Image" src="https://github.com/user-attachments/assets/1f5413e6-2ca0-40b5-9fbc-b50b28703c9a" />

### 앱 프론트엔드 & 백엔드
- Java + Android Studio로 백그라운드 BLE 스캔 모듈 구현
- 서버 백엔드 API 연결 및 데이터 바인딩

### 서버
- 안정적인 인프라 환경을 위해, Oracle Cloud Free Tier + Nginx로 서버 구축
- Python + FastAPI를 통한 기능별 API 구현
- MySQL을 통한 데이터베이스 작업 처리
- 복합 인덱스 적용을 통한 대용량 시계열 조회 쿼리 성능 최적화

### 테이블
<img width="2649" height="1375" alt="Image" src="https://github.com/user-attachments/assets/6f99d56a-dbe5-4861-a25b-cd04eb82f4e7" />

<br />

## 📌 API 엔드포인트
<img width="2737" height="1571" alt="Image" src="https://github.com/user-attachments/assets/638aa4ab-cf9e-4af7-add8-8337f475c7c7" />

<img width="2737" height="1174" alt="Image" src="https://github.com/user-attachments/assets/6cd59664-de1a-4b08-bff6-7cc796e57dd8" />

<br />

## 🔥 핵심 트러블 슈팅 

### 1. 라즈베리파이 전원 불량으로 인한 서버 다운 및 인프라 이전
- 상황: 라즈베리파이를 서버로 구동 시, 일정 시간이 지나면 무선 네트워크 연결이 끊기며 서버가 지속적으로 다운되는 현상 발생.
- 원인: 로그 분석 중 'hwmon hwmon1: Undervoltage detected' 경고 메시지 발견. 기존 저가형 USB-C 케이블의 얇은 두께로 인해 정격 전압(5V)이 공급되지 않아 무선 랜카드 칩셋이 오동작한 것으로 진단.
- 해결: 단기적으로는 짧고 두꺼운 고품질 케이블로 교체하여 전원을 안정화함. 이후 프로젝트의 24시간 가용성과 안정적인 서비스 제공을 위해 Oracle Cloud Free Tier 인프라로 서버를 완전히 이전 조치.
- 결과: 하드웨어 전원 이슈를 차단하고, 외부 환경 변화에 영향받지 않는 클라우드 서버 환경 구축 완료.

### 2. 비기능 요구사항(3초 이내 DB 저장) 달성을 위한 선제적 쿼리 튜닝
- 상황: 시스템 설계 단계에서 대량의 캠퍼스 센서 시계열 데이터가 지속적으로 누적될 경우, '3초 이내 데이터 처리 및 DB 저장'이라는 시스템 비기능 요구사항을 충족하지 못할 잠재적 위험 인지.
- 원인: 데이터가 누적될수록 신규 센서 데이터 저장 시 기존 데이터와의 비교/검증 조회가 필수적인데, `건물 ID(building_id)`와 `측정 시간(created_at)` 조건에 인덱스가 없으면 테이블 전체를 탐색하게 되어 전체 저장 프로세스가 기하급수적으로 느려질 것으로 예측.
- 해결: 주로 함께 조건절에 사용되는 `(building_id, created_at)` 조합으로 복합 인덱스를 선제적으로 설계 및 적용. 카디널리티가 높은 컬럼을 전면에 배치하여 탐색 효율을 극대화함.
- 결과: Apache Bench를 통해 '동시 요청 50개 / 총 1000회 API 호출' 부하 환경에서 테스트를 진행한 결과, 실패 없이 모든 요청이 평균 0.583초 만에 처리 및 저장됨을 확인. 데이터 누적 및 동시 요청 환경에서도 3초 이내 처리 기준을 안정적으로 만족하며 비기능 요구사항 달성.

### 3. Well-known 포트 브루트포스(Brute Force) 공격 방어 및 Nginx 기반 인프라 보안 강화
- 상황: 서버 구축 초기, 기본 포트를 그대로 노출하여 운영하던 중 자동화된 봇에 의한 무차별 대입(Brute Force) 공격 로그가 대량으로 발생하며 서버 자원이 낭비되는 문제 인지.
- 원인: FastAPI 단독 구동 환경에서는 애플리케이션 레벨에서 IP 차단 및 악성 접근 로그 필터링을 효율적으로 처리하기 어렵고, `fail2ban` 같은 인프라 보안 툴과의 연동이 까다롭다는 한계 확인.
- 해결: 외부 노출 포트를 표준 Well-known 포트가 아닌 커스텀 포트로 변경하여 1차 타깃팅을 회피함. 이후 FastAPI 앞단에 Nginx를 리버스 프록시(Reverse Proxy)로 배치하여 Nginx의 액세스 로그를 기반으로 `fail2ban`이 악성 IP를 동적으로 탐지하고 방화벽(iptables)에서 자동 차단하도록 인프라 보안 아키텍처 구축.
- 결과: 인프라 구축 이후 브루트포스 공격 시도가 원천 차단되었으며, 애플리케이션 비즈니스 로직에 영향을 주지 않고 네트워크 레이어에서 보안 위협을 효율적으로 방어할 수 있게 됨.

## 👥 Team Members
<div align="center">

<table>
  <tr>
    <td align="center" width="220px">
      <a href="https://github.com/dydtmddl">
        <img src="https://github.com/dydtmddl.png" width="120px" alt="dydtmddl 프로필" style="border-radius: 50%;" />
        <br />
        <br />
        <strong>이승민</strong>
      </a>
      <br />
      <h5> [DB Manager] </h5>
    </td>
    <td align="center" width="220px">
      <a href="https://github.com/KimYeYoung125">
        <img src="https://github.com/KimYeYoung125.png" width="120px" alt="KimYeYoung125 프로필" style="border-radius: 50%;" />
        <br />
        <br />
        <strong>김예영</strong>
      </a>
      <br />
      <h5> [Server Administrator] </h5>
    </td>
  </tr>
  <tr>
    <td align="center" width="220px">
      <a href="https://github.com/KyeongTaek">
        <img src="https://github.com/user-attachments/assets/66b8985c-2e1e-4b3a-865d-488d0899a6bf" width="120px" alt="임경택 프로필" style="border-radius: 50%;" />
        <br />
        <br />
        <strong>임경택</strong>
      </a>
      <br />
      <h5> [App UI & Backend Co-work] </h5>
    </td>
    <td align="center" width="220px">
      <a href="https://github.com/serhong">
        <img src="https://github.com/serhong.png" width="120px" alt="serhong 프로필" style="border-radius: 50%;" />
        <br />
        <br />
        <strong>최준희</strong>
      </a>
      <br />
      <h5> [App Backend] </h5>
    </td>
  </tr>
</table>
</div>

<br />

## 🎯 테스트
<img width="2925" height="1574" alt="Image" src="https://github.com/user-attachments/assets/10693f5d-02f0-471d-a37c-2eddd26f90ce" />

<br />
