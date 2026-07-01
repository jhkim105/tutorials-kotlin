# Databricks CRUD 예제 애플리케이션

이 프로젝트는 Spring Boot & Kotlin 환경에서 공식 Databricks JDBC 드라이버를 사용하여 Databricks SQL Warehouse 또는 Cluster와 통신해 데이터를 생성, 조회, 수정, 삭제(CRUD)하는 표준 예제입니다.

---

## 1. 도메인 설계: IoT 디바이스 관리 (IoT Device Management)
대용량 분석 플랫폼인 Databricks에 걸맞게 비즈니스 및 IoT 분석 환경에서 널리 쓰이는 디바이스 상태 관리를 예제로 정의했습니다.

### 데이터 테이블 구조
*   **테이블명:** `iot_devices`
*   **컬럼 정보:**
    *   `device_id` (VARCHAR, PK): 디바이스 고유 UUID
    *   `device_name` (VARCHAR): 디바이스 이름
    *   `device_type` (VARCHAR): 디바이스 유형 (TEMPERATURE, PRESSURE, FLOW, HUMIDITY, VIBRATION)
    *   `status` (VARCHAR): 현재 상태 (ONLINE, OFFLINE, MAINTENANCE)
    *   `location` (VARCHAR): 물리 설치 위치
    *   `last_heartbeat` (TIMESTAMP): 최종 통신 시각

---

## 2. 기술 사양 (Tech Stack)
*   **Framework:** Spring Boot 3.3.x
*   **Language:** Kotlin 1.9.24 (Java 21)
*   **Database Client:** `NamedParameterJdbcTemplate`
*   **JDBC Driver:** `com.databricks:databricks-jdbc:3.4.1`
*   **Local DB:** H2 Database (In-Memory)
*   **Logging:** `KotlinLogging` (`io.github.oshai:kotlin-logging-jvm`)

---

## 3. 프로필별 아키텍처 구성

애플리케이션은 실행 프로필(`local` 또는 `databricks`)에 따라 다른 데이터소스 및 레포지토리 빈(Bean)을 사용하도록 설계되었습니다.

```mermaid
graph TD
    A[Client UI] --> B[DeviceController]
    B --> C[DeviceService]
    C --> D{Active Profile}
    D -- local --[x] E[H2DeviceRepository]
    D -- databricks --[x] F[DatabricksDeviceRepository]
    E --> G[(Local H2 DB)]
    F --> H[(Databricks Delta Table)]
```

### 3.1 `local` 프로필 (기본값)
- 로컬 개발 및 빠른 검증을 위해 H2 메모리 DB를 연결합니다.
- 데이터베이스 초기화(`DatabaseConfig.kt`)를 통해 `iot_devices` 테이블을 생성하고 더미 데이터를 삽입합니다.

### 3.2 `databricks` 프로필
- 실제 Databricks SQL Warehouse에 접속하여 Delta Lake 테이블에 CRUD 연산을 수행합니다.
- Databricks SQL에서 최적화된 ANSI SQL 쿼리를 사용합니다.

---

## 4. API 엔드포인트 명세

| Method | URI | Description | Request Body | Response Body |
| :--- | :--- | :--- | :--- | :--- |
| **GET** | `/api/devices` | 전체 디바이스 목록 조회 | - | `List<Device>` |
| **GET** | `/api/devices/{id}` | 디바이스 단건 조회 | - | `Device` |
| **POST** | `/api/devices` | 신규 디바이스 등록 | `DeviceRequest` | `Device` |
| **PUT** | `/api/devices/{id}` | 디바이스 정보 수정 | `DeviceRequest` | `Device` |
| **DELETE** | `/api/devices/{id}` | 디바이스 정보 삭제 | - | `Void` |

---

## 5. Databricks 계정 연결 가이드

실제 Databricks 환경과 연동하여 CRUD를 수행하려면 아래 단계를 따릅니다.

### 단계 1: Connection Details 확보하기
1. Databricks Workspace에 로그인합니다.
2. 사이드바에서 **SQL Warehouses** (또는 **Compute**) 메뉴를 선택합니다.
3. 연결하고자 하는 SQL Warehouse 또는 클러스터를 클릭합니다.
4. **Connection details** 탭으로 이동하여 아래 두 정보를 복사합니다:
   - **Server hostname** (예: `adb-xxxxxxxxxxxx.x.azuredatabricks.net`)
   - **HTTP path** (예: `/sql/1.0/warehouses/xxxxxxxxxxxx`)

### 단계 2: Personal Access Token (PAT) 발급하기
1. Databricks 우측 상단의 프로필 이름을 클릭하고 **User Settings**로 이동합니다.
2. **Developer** 탭 또는 **Developer Settings**로 이동하여 **Access tokens** 옆의 **Manage**를 클릭합니다.
3. **Generate new token** 버튼을 클릭하여 토큰을 발급받고 안전한 곳에 복사해 둡니다.

### 단계 3: JDBC URL 및 드라이버 설정하기

애플리케이션에 접속 정보 정보를 주입하는 방법은 두 가지가 있습니다.

#### 방법 A: 환경 변수 활용 (권장)
소스 코드에 계정 정보를 노출하지 않기 위해 터미널에 환경 변수를 지정합니다.
```bash
export DATABRICKS_JDBC_URL="jdbc:databricks://<Server-Hostname>:443/default;transportMode=http;ssl=1;httpPath=<HTTP-Path>;AuthMech=3;UID=token;PWD=<발급받은-Access-Token>"
```

#### 방법 B: 설정 파일 직접 수정
[application-databricks.yml](file:///Users/jihwankim/dev/my/tutorials-kotlin/databricks/src/main/resources/application-databricks.yml) 파일의 `spring.datasource.url` 값을 직접 입력합니다:
```yaml
spring:
  datasource:
    url: jdbc:databricks://<Server-Hostname>:443/default;transportMode=http;ssl=1;httpPath=<HTTP-Path>;AuthMech=3;UID=token;PWD=<발급받은-Access-Token>
```

### 단계 4: Databricks 프로필로 애플리케이션 실행
터미널에서 아래 명령어로 `databricks` 프로필을 활성화하여 구동합니다:
```bash
./gradlew bootRun --args='--spring.profiles.active=databricks'
```
*서버가 켜지면서 `DatabaseConfig` 내 코드가 실행되어 Databricks에 자동으로 `iot_devices` Delta 테이블을 검사 및 신규 생성합니다.*

---

## 6. 빌드 및 테스트 실행

### 테스트 실행
```bash
./gradlew test --no-daemon
```

### 로컬 테스트 기동 (H2 기반)
```bash
./gradlew bootRun
```
접속 주소: **[http://localhost:8080](http://localhost:8080)**
