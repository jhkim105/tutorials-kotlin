# ClickHouse Spring Boot 데모 어플리케이션 가이드

이 서브모듈은 Spring Boot 3.x 환경에서 ClickHouse 데이터베이스와의 고속 JDBC 연동을 구성하고, 대규모 이커머스 클릭스트림 로그(`ProductViewEvent`)를 수집 및 실시간 분석(OLAP)하는 데모 프로젝트입니다.

---

## 🛠️ 기술 스택 (Technology Stack)

* **Language:** Kotlin (1.9.25)
* **Framework:** Spring Boot (3.4.4), Spring JDBC (JdbcTemplate)
* **Build Tool:** Gradle with Kotlin DSL
* **Database Migration:** Flyway (with clickhouse-database plugin)
* **Testing:** Kotest (BehaviorSpec), MockK
* **Database Driver:** clickhouse-jdbc (0.6.3:all)

---

## 🏃 실행 및 검증 가이드 (Execution & Test)

이 디렉토리(`clickhouse/spring-clickhouse`)로 이동한 후 아래의 Gradle 명령어를 통해 구동합니다.

> [!IMPORTANT]
> 로컬 ClickHouse 서버(Docker Compose)가 사전에 [docker-compose.yml](../docker-clickhouse/docker-compose.yml)을 통해 실행 중이어야 합니다.

* **1. 전체 통합 테스트 실행:**
  ```bash
  ./gradlew test
  ```
  * 테스트 실행 시 `product_view_events` 테이블 스키마 마이그레이션이 자동 실행되며, Kotest 스펙을 통해 CRUD 및 OLAP 인기 상품 순위 집계 로직의 무결성을 검증합니다.

* **2. 로컬 어플리케이션 실행:**
  ```bash
  ./gradlew bootRun
  ```

---

## 🚀 실전 REST API 명세 및 실습

스프링 부트가 구동되면 아래의 REST 엔드포인트를 통해 실시간 상품 조회 스트림 데이터를 저장하고 분석 통계를 산출해볼 수 있습니다.

### 1. API 엔드포인트 명세서

| 기능 | HTTP 메서드 | 엔드포인트 URI | 요청 바디 (JSON) / 파라미터 |
| :--- | :--- | :--- | :--- |
| **로그 단건 저장** | `POST` | `/api/events` | `ProductViewEventRequest` |
| **전체 로그 조회** | `GET` | `/api/events` | - |
| **로그 상세 조회** | `GET` | `/api/events/{id}` | `id` (UUID 경로 변수) |
| **로그 단건 삭제** | `DELETE` | `/api/events/{id}` | `id` (UUID 경로 변수) |
| **실시간 인기 상품 집계** | `GET` | `/api/events/top` | `limit` (정수, 기본값 5) |

* **ProductViewEventRequest 스펙:**
  ```json
  {
    "productId": "PROD-A",
    "userId": "user-100",
    "price": 12000.00,
    "urlPath": "/products/PROD-A",
    "referrer": "google"
  }
  ```

### 2. cURL을 이용한 직접 호출 예시

1. **상품 상세 조회 클릭 스트림 등록:**
   ```bash
   curl -X POST http://localhost:8080/api/events \
     -H "Content-Type: application/json" \
     -d '{"productId":"PROD-A","userId":"user-100","price":12000.00,"urlPath":"/products/PROD-A","referrer":"google"}'
   ```

2. **실시간 가장 많이 조회된 인기 상품 OLAP 집계 (상위 3위):**
   ```bash
   curl -X GET "http://localhost:8080/api/events/top?limit=3"
   ```
   * **응답 예시:**
     ```json
     [
       { "productId": "PROD-A", "viewCount": 3 },
       { "productId": "PROD-B", "viewCount": 2 },
       { "productId": "PROD-C", "viewCount": 1 }
     ]
     ```

### 3. IntelliJ IDEA 전용 편리한 실습 방법
동일 디렉토리에 포함된 [api-test.http](api-test.http) 파일을 사용하세요. 
IntelliJ 내에서 녹색 화살표(▶) 버튼만 한 번씩 누르면 즉시 전체 API 동작 시나리오를 편리하게 실행하고 응답을 분석할 수 있습니다.
