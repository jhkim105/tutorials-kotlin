# Feature Specification: JPA Date & Time Types DB Persistence and API Serialization Comparison

**Feature Directory**: `specs/001-jpa-datetime`  
**Created**: 2026-10-09  
**Status**: Ready for Planning  
**Input**: JPA 시간 관련 타입이 DB 에 어떻게 저장되고, API 엔드포인트로 노출시 어떻게 노출되는지 비교해서 테스트해보려고 해 (`persistence/spring-jpa-datetime`)

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Java Date/Time Entity Persistence & DB Type Comparison (Priority: P1)

개발자는 JPA 엔티티에 정의된 현대적인 Java 8+ 시간 타입(`LocalDateTime`, `LocalDate`, `LocalTime`, `Instant`, `ZonedDateTime`, `OffsetDateTime`, `OffsetTime`)이 MySQL과 PostgreSQL에서 각각 어떤 컬럼 타입으로 DDL이 생성되고 실제 값이 어떤 시간대(UTC 또는 Local)로 저장/조회되는지 명확히 검증할 수 있어야 한다.

**Why this priority**:
JPA를 사용할 때 시간대(Timezone) 유실 및 DB 방언(Dialect)에 따른 자동 DDL 매핑 차이로 인한 버그가 빈번하므로, 각 DB별 저장 메커니즘을 정확히 파악하는 것이 PoC의 핵심 가치이다.

**Independent Test**:
MySQL 및 PostgreSQL 환경(Testcontainers 기반)에서 시간 엔티티를 생성 및 저장한 뒤, DB에 저장된 raw 값과 엔티티 복원 값을 비교 검증하여 단독으로 테스트할 수 있다.

**Acceptance Scenarios**:
1. **Given** `Asia/Seoul` 또는 특정 시간대를 가진 `ZonedDateTime`, `OffsetDateTime`, `Instant`, `LocalDateTime`, `LocalDate`, `LocalTime` 값을 가진 엔티티를 생성하고,
   **When** MySQL 또는 PostgreSQL에 저장하고 다시 조회할 때,
   **Then** 각 타입별 시간 값과 타임존 오프셋이 기대하는 변환 규칙(UTC 변환 여부 등)에 맞게 정상 저장 및 복원된다.
2. **Given** 시간 범위(`OffsetTime` 또는 `OffsetDateTime`) 검색 조건이 주어졌을 때,
   **When** Repository를 통해 between 쿼리를 실행하면,
   **Then** 해당 시간 범위 내의 레코드가 정상적으로 필터링되어 반환된다.

---

### User Story 2 - REST API JSON Serialization & Deserialization Comparison (Priority: P2)

클라이언트/프론트엔드 개발자 및 API 소비자는 시간 엔티티 조회/등록 REST API를 호출했을 때, 각 시간 타입이 JSON으로 직렬화되는 포맷(ISO-8601 표준 포맷, 나노초 정밀도, 오프셋 표기 등)과 요청 본문으로 전달된 시간 문자열의 역직렬화 결과를 확인할 수 있어야 한다.

**Why this priority**:
DB 저장뿐만 아니라 API 응답 시 클라이언트와의 시간 포맷 규약(Jackson 직렬화 설정)을 일관되게 설계하기 위한 레퍼런스를 제공한다.

**Independent Test**:
MockMvc 또는 WebTestClient를 통해 시간 엔티티 등록/조회 API를 호출하고 응답 JSON의 필드별 포맷 문자열을 검증한다.

**Acceptance Scenarios**:
1. **Given** 서버에 저장된 시간 엔티티가 존재할 때,
   **When** `GET /api/time-records/{id}`를 호출하면,
   **Then** 200 OK와 함께 각 시간 필드가 정해진 표준 포맷(ISO-8601 e.g. `2026-10-09T08:00:00+09:00`, `2026-10-09T08:00:00.123456`)의 JSON으로 반환된다.
2. **Given** 다양한 시간 포맷 문자열이 담긴 요청 바디를 준비하고,
   **When** `POST /api/time-records`를 호출하면,
   **Then** 정상 파싱되어 엔티티로 변환 및 저장된 후 201 Created가 반환된다.

---

### User Story 3 - Interactive Demo Web UI (Priority: P2)

개발자나 테스터는 브라우저를 통해 데모 웹 페이지에 접속하여 손쉽게 시간 데이터를 생성하고, API로 응답된 JSON 데이터와 실제 DB에 저장된 상태를 한눈에 직관적으로 비교·확인할 수 있어야 한다.

**Why this priority**:
Postman이나 cURL 없이도 브라우저에서 즉시 다양한 시간 타입 생성(현재 시간, 타임존 지정 등)과 조회 결과를 시각적으로 대조할 수 있어 PoC 및 데모 시연에 매우 유용하다.

**Independent Test**:
서버 실행 후 브라우저(`http://localhost:8080` 또는 `http://localhost:8081`)로 접속하여 생성 폼 제출 및 목록/상세 결과 테이블 조회가 정상 작동하는지 확인한다.

**Acceptance Scenarios**:
1. **Given** 데모 웹 페이지 접속 시,
   **When** "현재 시간으로 생성" 또는 "특정 타임존으로 생성" 버튼을 클릭하면,
   **Then** API가 호출되어 새 레코드가 생성되고 테이블에 JSON 응답 원문 및 필드별 값이 즉시 렌더링된다.

---

### User Story 4 - Multi-DB Docker Compose Environment (Priority: P2)

사용자는 Docker Compose를 통해 MySQL, PostgreSQL, 그리고 각각에 연결된 2개의 Spring Boot 앱(`app1`: MySQL 연결 8080포트, `app2`: PostgreSQL 연결 8081포트)을 원클릭으로 동시 구동할 수 있어야 하며, DB 포트(MySQL 3306, PostgreSQL 5432)를 외부에 노출하여 DataGrip 등 외부 GUI 툴에서 DB 내부 데이터를 직접 조회할 수 있어야 한다.

**Why this priority**:
동일한 코드베이스가 MySQL과 PostgreSQL 각각에서 어떻게 구동되고 데이터가 실제 테이블 컬럼에 어떻게 기록되는지 DataGrip과 브라우저에서 동시에 대조할 수 있다.

**Independent Test**:
`docker compose up -d` 구동 후 DataGrip으로 `localhost:3306` 및 `localhost:5432`에 접속하고, 브라우저로 `localhost:8080`과 `localhost:8081`에 각각 접속하여 동작을 검증한다.

**Acceptance Scenarios**:
1. **Given** `docker compose up -d`를 실행했을 때,
   **When** 컨테이너들이 모두 구동되면,
   **Then** `app1`(8080, MySQL)과 `app2`(8081, Postgres)가 각 DB와 정상 연결되어 헬스체크 및 데모 페이지가 응답한다.
2. **Given** DataGrip 또는 외부 DB 클라이언트에서 `localhost:3306`(MySQL) 및 `localhost:5432`(PostgreSQL)로 연결할 때,
   **When** 생성된 `time_record` 테이블의 컬럼 타입(schema)과 SELECT 쿼리를 실행하면,
   **Then** 각 DB의 raw 컬럼 타입 및 저장된 시간 데이터가 조회된다.

---

### Edge Cases

- DB 타임존(DB Server Timezone)과 JVM 시스템 타임존(`Asia/Seoul` vs `UTC`)이 다를 때의 저장 및 변환 동작
- 서머타임(DST) 적용 타임존(e.g., `America/New_York`, `Europe/London`)을 사용한 `ZonedDateTime` 처리
- MySQL 5.7 vs 8.0 이상에서 fractional seconds (나노초/마이크로초) 절삭(Truncation) 또는 반올림 이슈
- `null` 값이 허용된 시간 필드의 직렬화/역직렬화 및 쿼리 동작

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: 시스템은 다양한 Java 시간 타입을 포함하는 `TimeRecord` JPA 엔티티를 제공해야 한다.
  - 지원 타입 (7종): `LocalDateTime`, `LocalDate`, `LocalTime`, `Instant`, `ZonedDateTime`, `OffsetDateTime`, `OffsetTime`.
- **FR-002**: 시스템은 `TimeRecord`의 등록, 단건 조회, 목록 조회, 시간 범위 검색(`OffsetTime` 또는 `OffsetDateTime` between) 기능을 제공해야 한다.
- **FR-003**: 시스템은 `TimeRecord`를 JSON으로 반환하는 REST API 컨트롤러를 제공해야 한다.
- **FR-004**: 시스템은 브라우저에서 API를 호출하고 결과를 시각적으로 확인할 수 있는 웹 데모 페이지(`index.html`)를 정적 리소스로 제공해야 한다.
- **FR-005**: 시스템은 Spring Profile(`mysql`, `postgres`)을 통해 대상 데이터베이스 접속 및 설정을 독립적으로 전환할 수 있어야 한다.
- **FR-006**: 시스템은 `docker-compose.yml` 및 `Dockerfile`을 제공하여:
  - MySQL 컨테이너 (포트 `3306` 노출)
  - PostgreSQL 컨테이너 (포트 `5432` 노출)
  - `app1` (포트 `8080`, MySQL 프로파일 활성화)
  - `app2` (포트 `8081`, PostgreSQL 프로파일 활성화)
  를 한 번에 실행하고 DataGrip 등 외부 DB 클라이언트에서 접속할 수 있도록 지원해야 한다.
- **FR-007**: 시스템은 Testcontainers를 활용하여 로컬 DB 설치 없이도 MySQL 및 PostgreSQL 상에서 완벽히 격리된 통합 테스트를 실행할 수 있어야 한다.
- **FR-008**: Kotest BDD(`BehaviorSpec`) 기반 테스트를 통해 각 시간 타입별 DB 저장값 및 JSON 출력 결과를 명확한 표와 단언문으로 검증하고 로그로 출력해야 한다.

### Key Entities

- **TimeRecord**:
  - `id`: 고유 식별자 (UUID 문자열)
  - `title`: 테스트 시나리오 식별명 (e.g., "Default Now", "Specific Timezone")
  - `localDateTime`: `java.time.LocalDateTime`
  - `localDate`: `java.time.LocalDate`
  - `localTime`: `java.time.LocalTime`
  - `instant`: `java.time.Instant`
  - `zonedDateTime`: `java.time.ZonedDateTime`
  - `offsetDateTime`: `java.time.OffsetDateTime`
  - `offsetTime`: `java.time.OffsetTime`
  - `createdAt`, `updatedAt`: 감사용 일시

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 모든 지원 시간 타입(7종: `LocalDateTime`, `LocalDate`, `LocalTime`, `Instant`, `ZonedDateTime`, `OffsetDateTime`, `OffsetTime`)에 대한 DB 저장 및 조회 검증 테스트가 100% 통과한다.
- **SC-002**: MySQL 및 PostgreSQL 양쪽 환경에서 Testcontainers 기반 Kotest 테스트 스위트가 `./gradlew test`로 에러 없이 완주된다.
- **SC-003**: REST API를 통한 시간 엔티티 직렬화 결과가 ISO-8601 표준 포맷 및 기대 타임존 오프셋과 일치함을 테스트로 검증한다.
- **SC-004**: 웹 데모 페이지(`localhost:8080`, `localhost:8081`)에서 시간 레코드 생성 및 실시간 조회가 정상 작동한다.
- **SC-005**: `docker compose up -d` 실행 시 MySQL(3306), Postgres(5432), app1(8080), app2(8081)가 정상 기동되고 DataGrip에서 양쪽 DB로 직접 접속 가능하다.
- **SC-006**: README 및 테스트 로그에 MySQL vs PostgreSQL 간의 DDL 타입 매핑 및 시간 저장 차이점이 일목요연하게 정리된다.

---

## Assumptions

1. **DB 지원 방식 (Profile 분리 + Docker Compose Multi-App)**:
   - 애플리케이션 코드는 `mysql`, `postgres` 프로파일로 깔끔하게 분리하고,
   - Docker Compose를 통해 `app1`(mysql), `app2`(postgres) 두 인스턴스를 각각 다른 포트로 동시 실행하여 실시간 비교 환경을 제공한다.
2. **지원 시간 타입**:
   - 레거시 `Date`는 제외하고, 현대적인 `java.time` 표준 타입 7종(`LocalDateTime`, `LocalDate`, `LocalTime`, `Instant`, `ZonedDateTime`, `OffsetDateTime`, `OffsetTime`)을 대상으로 한다.
3. **프로젝트 위치**:
   - 모노레포 내 `persistence/spring-jpa-datetime` 디렉토리에 Gradle 서브프로젝트로 구성한다.
4. **기술 스택**:
   - Kotlin 1.9+, Spring Boot 3.x, Spring Data JPA, `kotlin-logging`, `Kotest` (`BehaviorSpec`), MockK, Testcontainers (MySQL & PostgreSQL), Docker Compose.
