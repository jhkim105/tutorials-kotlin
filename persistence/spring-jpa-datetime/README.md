# Spring Data JPA Date & Time Types Persistence and Serialization

JPA(Hibernate 6.x)에서 Java 8+ 현대적 날짜/시간 타입(`java.time.*`) 7종이 **MySQL**과 **PostgreSQL**에서 각각 어떤 DDL 컬럼 타입으로 매핑되고, 실제 DB에 물리적으로 어떻게 저장되며, REST API(Jackson)로 어떻게 직렬화/역직렬화되는지 비교·검증하는 레퍼런스 프로젝트입니다.

---

## 1. 지원하는 Java 8+ 시간 타입 (7종)

| Java Type | 개념 및 특징 |
|:---|:---|
| `LocalDateTime` | 타임존 오프셋이 없는 날짜와 시간 (`2026-10-17T20:30:00.123456`) |
| `LocalDate` | 날짜 전용 (`2026-10-17`) |
| `LocalTime` | 시간 전용 (`20:30:00.123456`) |
| `Instant` | UTC 기준의 절대 시점 타임스탬프 (`2026-10-17T11:30:00.123456Z`) |
| `ZonedDateTime` | 타임존 규칙 및 일광절약시간(DST)을 포함한 일시 (`2026-10-17T20:30:00.123456+09:00[Asia/Seoul]`) |
| `OffsetDateTime` | UTC 기준 오프셋만 고정된 일시 (`2026-10-17T20:30:00.123456+09:00`) |
| `OffsetTime` | UTC 기준 오프셋을 포함한 시간 (`20:30:00.123456+09:00`) |

---

## 2. 핵심 동작 분석 및 검증 결과

### 💡 왜 테스트 화면에서 뉴욕 시간(-04:00)을 저장했는데 DB에 UTC 시간이 들어가고 응답이 `Z`로 바뀔까요?

JPA 엔티티에 뉴욕 현지 시각 `2026-10-09 01:30:00 -04:00`을 담아 저장했을 때의 실제 동작 흐름:

```text
[Java 엔티티]
OffsetDateTime: 2026-10-09T01:30:00.123456-04:00 (뉴욕 현지 시각)
      ↓
[Hibernate 6 DDL & 저장]
MySQL 컬럼 타입은 오프셋을 저장할 수 없는 DATETIME(6)!
따라서 Hibernate가 타임존(-04:00)을 버리고 "UTC 절대 시점(05:30:00)"으로 정규화하여 저장.
      ↓
[DB 실제 저장값 (Raw)]
offset_date_time: "2026-10-09 05:30:00.123456" (원래 입력한 01:30과 -04:00 오프셋 완전 소실)
      ↓
[SELECT 및 REST API JSON 응답]
"offsetDateTime": "2026-10-09T05:30:00.123456Z" (원래 타임존 복원 불가능, UTC로 응답)
```

---

### DB별 DDL 컬럼 타입 및 저장 메커니즘 대조

| Java Type | MySQL 8.4 DDL 타입 | PostgreSQL 16 DDL 타입 | 실제 물리 DB 저장 및 타임존 보존 동작 |
|:---|:---|:---|:---|
| `LocalDateTime` | `DATETIME(6)` | `TIMESTAMP(6) WITHOUT TIME ZONE` | 타임존 없이 입력된 일시 그대로 저장 |
| `LocalDate` | `DATE` | `DATE` | 연-월-일만 저장 |
| `LocalTime` | `TIME(6)` | `TIME(6) WITHOUT TIME ZONE` | 시-분-초.마이크로초 저장 |
| `Instant` | `DATETIME(6)` | `TIMESTAMP(6) WITH TIME ZONE` | **PG**: UTC로 변환하여 TIMESTAMPTZ로 보존.<br>**MySQL**: 타임존 개념 없는 DATETIME에 UTC 일시만 기록. |
| `ZonedDateTime` | `DATETIME(6)` | `TIMESTAMP(6) WITH TIME ZONE` | **공통**: 기본 단일 컬럼 매핑 시 `Asia/Seoul` 같은 타임존 ID는 DB에 남지 않고 해당 시점의 UTC 일시로 정규화되어 **타임존 명칭 소실**. |
| `OffsetDateTime` | `DATETIME(6)` | `TIMESTAMP(6) WITH TIME ZONE` | **MySQL**: 오프셋(`+09:00`, `-04:00`)이 완전히 유실되고 UTC 일시로 변환 저장됨.<br>**PG**: TIMESTAMPTZ로 시점을 정확히 보존. |
| `OffsetTime` | `TIME(6)` | `TIME(6) WITH TIME ZONE` | **PG**: TIMETZ로 오프셋 보존.<br>**MySQL**: 오프셋 유실 후 TIME(6)으로 기록. |

---

## 3. 실무 개발자를 위한 가이드라인 (Best Practices)

1. **사용자가 입력한 원래 타임존/오프셋을 보존해야 하는 경우 (예: 항공편 출발지 현지 시각, 병원 예약 등)**:
   - MySQL 환경에서 `OffsetDateTime`이나 `ZonedDateTime` 컬럼 하나만 두면 타임존 정보가 유실됩니다.
   - **방법 A (권장)**: `LocalDateTime` 컬럼 + 타임존 문자열(`VARCHAR(64)`, e.g., `Asia/Seoul`) 컬럼을 분리하여 저장.
   - **방법 B (Hibernate 6)**: `@TimeZoneStorage(TimeZoneStorageType.COLUMN)` 애너테이션을 사용하여 오프셋을 별도 컬럼에 자동 분리 저장.
2. **글로벌 이벤트 로그, 감사 일시(생성일/수정일), 결제 시각**:
   - `Instant` 타입을 사용하는 것이 가장 이상적입니다. (타임존 혼란 없이 UTC 절대 시점만 명확하게 표현).
3. **단순 현지 날짜/시간 (달력, 알람 시간)**:
   - 타임존 변환이 개입되면 안 되는 데이터는 `LocalDate`, `LocalTime`, `LocalDateTime`을 사용합니다.

---

## 4. 실행 및 데모 확인

### 원클릭 실행 (JAR 빌드 + Docker Compose 동시 구동)
```bash
cd persistence/spring-jpa-datetime
./run.sh
```

또는 수동 실행:
```bash
cd persistence/spring-jpa-datetime
./gradlew bootJar -x test
docker compose up -d --build
```

### 접속 주소

| 서비스 | 주소 | 연동 DB | 주요 기능 |
|:---|:---|:---|:---|
| **App 1 (MySQL)** | `http://localhost:8080` | MySQL 8.4 (포트 3306) | 실시간 현재 시각 생성 & MySQL DDL/저장값 대조 |
| **App 2 (PostgreSQL)** | `http://localhost:8081` | PostgreSQL 16 (포트 5432) | 실시간 현재 시각 생성 & Postgres TIMESTAMPTZ 대조 |

---

## 5. Web UI 기능 안내

브라우저(`http://localhost:8080` 또는 `http://localhost:8081`)에 접속하면:

1. **⚡ 현재 시각(Now) 기준 레코드 생성**:
   - **Asia/Seoul (+09:00)**: 클릭한 시점의 현재 시각을 KST 기준으로 엔티티에 담아 저장합니다.
   - **UTC (+00:00)**: 클릭한 시점의 현재 시각을 UTC 기준으로 저장합니다.
   - **New York (EDT/EST)**: 클릭한 시점의 현재 시각을 뉴욕 현지 시각 및 서머타임 오프셋을 반영하여 저장합니다.
2. **🎯 고정 시점 대조군 (2026-10-09 05:30:00 UTC)**:
   - 전 세계에서 동일한 절대 순간을 각 타임존에서 보냈을 때 DB에 어떻게 기록되는지 비교할 수 있는 레퍼런스 픽스처입니다.
3. **🔍 Physical Database Raw Schema & Value Inspector**:
   - `Inspect Raw DB` 버튼 클릭 시, Spring Data JPA Entity 값과 실제 JDBC 메타데이터(DDL 컬럼 타입), DB 테이블에 실제로 쓰여진 Raw 문자열, JSON 직렬화 결과를 1:1로 비교할 수 있습니다.
