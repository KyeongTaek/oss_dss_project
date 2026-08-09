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
| 분야 | 기술 |
|------|------|
| **언어** | ![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white) ![Python](https://img.shields.io/badge/python-3670A0?style=for-the-badge&logo=python&logoColor=ffdd54)|
| **버전관리 & 협업** | ![Git](https://img.shields.io/badge/git-%23F05033.svg?style=for-the-badge&logo=git&logoColor=white) ![GitHub](https://img.shields.io/badge/github-%23121011.svg?style=for-the-badge&logo=github&logoColor=white) |
| **앱 UI & 백엔드** | ![Android Studio](https://img.shields.io/badge/Android%20Studio-3DDC84.svg?style=for-the-badge&logo=android-studio&logoColor=white) |
| **데이터베이스** | ![MySQL](https://img.shields.io/badge/mysql-4479A1.svg?style=for-the-badge&logo=mysql&logoColor=white) |
| **서버** | ![FastAPI](https://img.shields.io/badge/FastAPI-005571.svg?style=for-the-badge&logo=fastapi) ![Oracle](https://img.shields.io/badge/Oracle-F80000?style=for-the-badge&logo=oracle&logoColor=white) ![Nginx](https://img.shields.io/badge/nginx-%23009639.svg?style=for-the-badge&logo=nginx&logoColor=white) |

## 🔥 아키텍처 개요
<img width="1658" height="1253" alt="Image" src="https://github.com/user-attachments/assets/1f5413e6-2ca0-40b5-9fbc-b50b28703c9a" />

### 앱 프론트엔드 & 백엔드
- Java + Android Studio로 백그라운드 BLE 스캔 모듈 구현
- 서버 백엔드 API 연결 및 데이터 바인딩

### 서버
- Oracle + Nginx로 서버 구축
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
      <h5> [App UI] </h5>
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
