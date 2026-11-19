# Research: JPA Date & Time Types DB Persistence and Serialization

**Feature Directory**: `specs/20261009-jpa-datetime`  
**Date**: 2026-10-09  
**Status**: Completed  

---

## 1. Java 8+ DateTime Mapping in JPA / Hibernate 6.x (Spring Boot 3.x)

### Research Question
How do the 7 modern Java time types map to MySQL and PostgreSQL column types and handle timezone offsets under Hibernate 6.x?

### Decision
Support the 7 modern Java time types:
1. `java.time.LocalDateTime`
2. `java.time.LocalDate`
3. `java.time.LocalTime`
4. `java.time.Instant`
5. `java.time.ZonedDateTime`
6. `java.time.OffsetDateTime`
7. `java.time.OffsetTime`

Standard Hibernate 6 DDL and mapping behaviors:
- **MySQL 8.0+**:
  - `LocalDateTime` -> `DATETIME(6)` (No timezone awareness, stored as given)
  - `LocalDate` -> `DATE`
  - `LocalTime` -> `TIME(6)`
  - `Instant` -> `DATETIME(6)` (Stored in UTC / JVM normalized instant)
  - `OffsetDateTime` -> `DATETIME(6)` (Hibernate 6 defaults to `NORMALIZE` / UTC instant conversion unless configured)
  - `ZonedDateTime` -> `DATETIME(6)` (Normalized instant, ZoneId/ZoneOffset string is not stored in standard single column)
  - `OffsetTime` -> `TIME(6)` (Normalized time)
- **PostgreSQL 16+**:
  - `LocalDateTime` -> `TIMESTAMP(6) WITHOUT TIME ZONE`
  - `LocalDate` -> `DATE`
  - `LocalTime` -> `TIME(6) WITHOUT TIME ZONE`
  - `Instant` -> `TIMESTAMP(6) WITH TIME ZONE` (TIMESTAMPTZ - PG stores in UTC, displays in session timezone)
  - `OffsetDateTime` -> `TIMESTAMP(6) WITH TIME ZONE` (TIMESTAMPTZ - offset is preserved as point-in-time instant, PG displays in session tz)
  - `ZonedDateTime` -> `TIMESTAMP(6) WITH TIME ZONE` (TIMESTAMPTZ - zone identifier itself is not preserved in single column)
  - `OffsetTime` -> `TIME(6) WITH TIME ZONE` (TIMETZ)

### Rationale
- Hibernate 6 in Spring Boot 3 has native support for `Instant`, `OffsetDateTime`, and `ZonedDateTime`.
- Understanding how MySQL (which lacks native `TIMESTAMPTZ` and timezone column support) differs from PostgreSQL (`TIMESTAMPTZ` and `TIMETZ`) is crucial for developers designing cross-database architectures.
- Direct raw SQL queries (via `JdbcTemplate`) will be used alongside JPA entity loading in tests to prove exactly what is stored physically in the database columns vs what Hibernate reconstructs into Java objects.

### Alternatives Considered
- Legacy `java.util.Date`, `java.sql.Timestamp`, `java.util.Calendar`: Rejected because modern applications exclusively use `java.time.*` (JSR-310), and legacy classes are error-prone and deprecated in modern design.
- Custom Hibernate `@TimeZoneStorage(TimeZoneStorageType.COLUMN)`: Evaluated, but standard default single-column persistence is prioritized as the primary baseline comparison. Optional column-based storage can be documented in notes.

---

## 2. Jackson Serialization & API Representation

### Research Question
How should Jackson (Spring Boot 3 default `JavaTimeModule`) serialize and deserialize these time types in REST endpoints?

### Decision
- Use Spring Boot's built-in `jackson-datatype-jsr310` (`JavaTimeModule`) configured with standard ISO-8601 string serialization:
  - `spring.jackson.serialization.write-dates-as-timestamps: false` (default in Spring Boot 3)
  - `spring.jackson.deserialization.adjust-dates-to-context-time-zone: false` (to preserve timezone offsets on `OffsetDateTime` and `ZonedDateTime` during JSON deserialization)
- Serialization Output Examples:
  - `LocalDateTime`: `"2026-10-09T08:30:00.123456"`
  - `LocalDate`: `"2026-10-09"`
  - `LocalTime`: `"08:30:00.123456"`
  - `Instant`: `"2026-10-08T23:30:00.123456Z"`
  - `OffsetDateTime`: `"2026-10-09T08:30:00.123456+09:00"`
  - `ZonedDateTime`: `"2026-10-09T08:30:00.123456+09:00"` (or with zone name like `[Asia/Seoul]`)
  - `OffsetTime`: `"08:30:00.123456+09:00"`

### Rationale
- Standard ISO-8601 string formatting prevents string mismatch across web clients and API consumers.
- Disabling timestamp timestamp integers/floats avoids precision loss in JavaScript clients (JavaScript `Number` precision issues with nanoseconds).

### Alternatives Considered
- Custom serializers for each field: Rejected to keep the PoC representative of real-world Spring Boot defaults.

---

## 3. Database Isolation and Profile Strategy

### Research Question
How to structure the application configuration to run seamlessly against MySQL and PostgreSQL in both standalone mode, Testcontainers, and Docker Compose?

### Decision
- Use Spring Profiles:
  - `application.yml`: Common settings (JPA open-in-view, Jackson formats, ddl-auto default)
  - `application-mysql.yml`: `spring.datasource` for MySQL, MySQL dialect, port 8080 default
  - `application-postgres.yml`: `spring.datasource` for PostgreSQL, PostgreSQL dialect, port 8081 default
- Testcontainers:
  - Use `MySQLContainer("mysql:8.4")` and `PostgreSQLContainer("postgres:16-alpine")` dynamically in Kotest tests using `@Testcontainers` or DynamicPropertySource.

### Rationale
- Allows launching `app1` with `--spring.profiles.active=mysql` and `app2` with `--spring.profiles.active=postgres` without changing code or artifact packaging.
- Testcontainers provide zero-dependency local testing without pre-installed database engines.

### Alternatives Considered
- Separate modules `persistence/spring-jpa-datetime-mysql` and `persistence/spring-jpa-datetime-postgres`: Rejected as unnecessary duplication; profile-driven multi-DB demo within single module is cleaner and easier to maintain.

---

## 4. Web UI & Visualization Architecture

### Research Question
How should the interactive web demo UI be built and served?

### Decision
- Static HTML/JS file at `src/main/resources/static/index.html` with modern responsive styling (Tailwind CSS via CDN) and Vanilla JS.
- Features:
  - Preset Creation Buttons ("Create with Now (Asia/Seoul)", "Create with UTC", "Create with Custom Timezone/DST")
  - Live API Request/Response Inspector (displays raw JSON payload)
  - Comparison Table: Displays (1) Entity Field Name, (2) Java Type, (3) Input Value, (4) DB Schema Column Type, (5) DB Stored Raw Value (via metadata query), (6) Restored Entity Value.
  - Time Range Filter (Search records between specific `OffsetTime` or `OffsetDateTime`).

### Rationale
- Zero frontend build toolchain required (`node`/`npm`/`webpack` not needed), keeping Gradle build lightweight and pure Kotlin/JVM.
- Immediately accessible when opening `http://localhost:8080` (MySQL) and `http://localhost:8081` (PostgreSQL).

---

## 5. Test Architecture & Kotest BDD Design

### Research Question
How to verify DB persistence and API serialization with Kotest and Testcontainers?

### Decision
- Frameworks: Kotest (`BehaviorSpec`), Spring Boot Test, Testcontainers, `kotlin-logging`.
- Test Specs:
  1. `TimeRecordPersistenceMysqlSpec`: Runs with MySQL Testcontainer, verifies JPA save/find, executes raw SQL query to verify actual DB column types & string formats, and logs comparative output table.
  2. `TimeRecordPersistencePostgresSpec`: Runs with PostgreSQL Testcontainer, verifies `TIMESTAMPTZ`/`TIMETZ` behavior, raw SQL values, and range queries.
  3. `TimeRecordApiSpec`: MockMvc / WebTestClient test verifying REST API serialization, deserialization, and status codes.

### Rationale
- Meets Constitution Principle 3 (BDD-style Kotest & Testcontainers) and provides clear, executable proof of behavior.
