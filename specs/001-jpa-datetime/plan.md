# Implementation Plan: JPA Date & Time Types DB Persistence and API Serialization Comparison

**Branch**: `001-jpa-datetime` | **Date**: 2026-10-09 | **Spec**: [spec.md](file:///Users/jihwankim/workspace/tutorials-kotlin/specs/001-jpa-datetime/spec.md)

**Input**: Feature specification from `specs/001-jpa-datetime/spec.md`

## Summary

Build a comprehensive reference module (`persistence/spring-jpa-datetime`) demonstrating and verifying how modern Java 8+ Date/Time types (7 types: `LocalDateTime`, `LocalDate`, `LocalTime`, `Instant`, `ZonedDateTime`, `OffsetDateTime`, `OffsetTime`) are persisted in relational databases (MySQL vs PostgreSQL) and serialized/deserialized via Spring Boot 3 REST APIs. Provide an interactive Web UI (`index.html`), Docker Compose multi-DB environment (`app1`: MySQL on 8080, `app2`: Postgres on 8081), and Kotest BDD test suites with Testcontainers.

## Technical Context

**Language/Version**: Kotlin 1.9.25, Java 17

**Primary Dependencies**:
- Spring Boot 3.4.4 (`spring-boot-starter-data-jpa`, `spring-boot-starter-web`)
- Jackson Kotlin Module (`com.fasterxml.jackson.module:jackson-module-kotlin`, `jackson-datatype-jsr310`)
- Kotlin Logging (`io.github.oshai:kotlin-logging-jvm:7.0.0`)
- Database Drivers: MySQL Connector/J (`com.mysql:mysql-connector-j`), PostgreSQL JDBC (`org.postgresql:postgresql`)

**Storage**:
- MySQL 8.4
- PostgreSQL 16

**Testing**:
- Kotest 5.9.1 (`BehaviorSpec`, `kotest-runner-junit5`, `kotest-assertions-core`, `kotest-extensions-spring`)
- Spring Boot Starter Test (`MockMvc`)
- Testcontainers (`testcontainers:mysql`, `testcontainers:postgresql`)
- SpringMockk / MockK

**Target Platform**: JVM (macOS / Linux / Docker)

**Project Type**: Spring Boot Web & Persistence Reference Subproject

**Performance Goals**: N/A (PoC & Reference Verification)

**Constraints**:
- Zero external pre-installed database required for `./gradlew test` (enforced via Testcontainers).
- Support one-click local multi-DB demonstration via `docker compose up -d`.

**Scale/Scope**: Self-contained standalone Gradle subproject under `persistence/spring-jpa-datetime`.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Requirement | Status | Notes |
|:---|:---|:---:|:---|
| **1. Kotlin & Spring Boot Standards** | Kotlin Idioms, Spring Boot 3.x, Gradle Kotlin DSL (`build.gradle.kts`) | **PASS** | Uses Kotlin 1.9.25, Spring Boot 3.4.4, Gradle Kotlin DSL, no Java boilerplate |
| **2. Kotlin-friendly Logging** | Use `kotlin-logging` (`io.github.oshai:kotlin-logging-jvm`) | **PASS** | `KotlinLogging.logger {}` used across services and specs |
| **3. BDD-Style Testing & Verification** | Kotest (`BehaviorSpec`), MockK, Testcontainers for reproducible verification | **PASS** | MySQL & PostgreSQL specs written in `BehaviorSpec` with Testcontainers |
| **4. Modular PoC Structure** | Self-contained runnable Gradle subproject under `persistence/` | **PASS** | Located in `persistence/spring-jpa-datetime`, runnable via `./gradlew bootRun` and `./gradlew test` |
| **5. Spec-Driven Knowledge Archiving** | Clear objectives, acceptance criteria, executable test verification | **PASS** | Detailed `data-model.md`, `research.md`, `quickstart.md`, and comparative reporting |

## Project Structure

### Documentation (this feature)

```text
specs/001-jpa-datetime/
├── spec.md              # Feature specification
├── plan.md              # Implementation plan (this file)
├── research.md          # Phase 0 research findings
├── data-model.md        # Phase 1 data model & schema definitions
├── quickstart.md        # Phase 1 validation & run guide
├── contracts/           # Phase 1 interface contracts
│   └── time-records-api.yaml
└── tasks.md             # Phase 2 task checklist (generated via /speckit-tasks)
```

### Source Code Layout

```text
persistence/spring-jpa-datetime/
├── build.gradle.kts
├── settings.gradle.kts
├── Dockerfile
├── compose.yml
├── README.md
├── src/
│   ├── main/
│   │   ├── kotlin/jhkim105/tutorials/jpadatetime/
│   │   │   ├── JpaDateTimeApplication.kt
│   │   │   ├── domain/
│   │   │   │   ├── TimeRecord.kt
│   │   │   │   └── TimeRecordRepository.kt
│   │   │   ├── dto/
│   │   │   │   ├── CreateTimeRecordRequest.kt
│   │   │   │   ├── TimeRecordResponse.kt
│   │   │   │   └── TimeRecordRawView.kt
│   │   │   ├── service/
│   │   │   │   ├── TimeRecordService.kt
│   │   │   │   └── DbMetadataService.kt
│   │   │   └── controller/
│   │   │       └── TimeRecordController.kt
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-mysql.yml
│   │       ├── application-postgres.yml
│   │       └── static/
│   │           └── index.html
│   └── test/
│       └── kotlin/jhkim105/tutorials/jpadatetime/
│           ├── TimeRecordPersistenceMysqlSpec.kt
│           ├── TimeRecordPersistencePostgresSpec.kt
│           └── TimeRecordApiSpec.kt
```

**Structure Decision**:
Placed as a standalone Gradle subproject `persistence/spring-jpa-datetime` consistent with existing persistence tutorials (`persistence/spring-jpa-postgresql`, `persistence/spring-jpa`, etc.).

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|:---|:---|:---|
| None | All designs adhere strictly to the repository constitution and modular architecture | N/A |
