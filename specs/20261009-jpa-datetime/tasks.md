# Tasks: JPA Date & Time Types DB Persistence and Serialization

**Feature Directory**: `specs/20261009-jpa-datetime`  
**Target Subproject**: `persistence/spring-jpa-datetime`  
**Date**: 2026-10-09  
**Status**: Ready for Implementation  

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization, Gradle configuration, and Spring Boot profile setup.

- [ ] T001 Initialize project directory structure in `persistence/spring-jpa-datetime/`
- [ ] T002 Configure Gradle build script with Kotlin 1.9.25, Spring Boot 3.4.4, JPA, Kotest, and Testcontainers in `persistence/spring-jpa-datetime/build.gradle.kts`
- [ ] T003 [P] Create subproject settings script in `persistence/spring-jpa-datetime/settings.gradle.kts`
- [ ] T004 [P] Configure common application properties in `persistence/spring-jpa-datetime/src/main/resources/application.yml`
- [ ] T005 [P] Configure MySQL datasource and JPA dialect profile in `persistence/spring-jpa-datetime/src/main/resources/application-mysql.yml`
- [ ] T006 [P] Configure PostgreSQL datasource and JPA dialect profile in `persistence/spring-jpa-datetime/src/main/resources/application-postgres.yml`
- [ ] T007 Create Spring Boot application entry point in `persistence/spring-jpa-datetime/src/main/kotlin/jhkim105/tutorials/jpadatetime/JpaDateTimeApplication.kt`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core entity, repository, and DTOs required across all user stories.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [ ] T008 Create `TimeRecord` JPA entity with all 7 Java 8+ Date/Time types in `persistence/spring-jpa-datetime/src/main/kotlin/jhkim105/tutorials/jpadatetime/domain/TimeRecord.kt`
- [ ] T009 Create `TimeRecordRepository` with CRUD and time range queries in `persistence/spring-jpa-datetime/src/main/kotlin/jhkim105/tutorials/jpadatetime/domain/TimeRecordRepository.kt`
- [ ] T010 [P] Create DTOs (`CreateTimeRecordRequest`, `TimeRecordResponse`, `TimeRecordRawView`) in `persistence/spring-jpa-datetime/src/main/kotlin/jhkim105/tutorials/jpadatetime/dto/TimeRecordDtos.kt`

**Checkpoint**: Core domain and persistence model ready.

---

## Phase 3: User Story 1 - Java Date/Time Entity Persistence & DB Type Comparison (Priority: P1) 🎯 MVP

**Goal**: Verify persistence, DDL column type mapping, and timezone handling for 7 Java date/time types across MySQL and PostgreSQL.

**Independent Test**: Run Kotest BDD tests (`TimeRecordPersistenceMysqlSpec` and `TimeRecordPersistencePostgresSpec`) with Testcontainers to verify raw SQL column types and restored entity values.

### Tests for User Story 1

- [ ] T011 [P] [US1] Create Kotest `BehaviorSpec` integration test with MySQL Testcontainer verifying 7 date/time types and raw column mapping in `persistence/spring-jpa-datetime/src/test/kotlin/jhkim105/tutorials/jpadatetime/TimeRecordPersistenceMysqlSpec.kt`
- [ ] T012 [P] [US1] Create Kotest `BehaviorSpec` integration test with PostgreSQL Testcontainer verifying TIMESTAMPTZ/TIMETZ types and between queries in `persistence/spring-jpa-datetime/src/test/kotlin/jhkim105/tutorials/jpadatetime/TimeRecordPersistencePostgresSpec.kt`

### Implementation for User Story 1

- [ ] T013 [US1] Implement `DbMetadataService` to inspect physical column types and query raw JDBC values via `JdbcTemplate` in `persistence/spring-jpa-datetime/src/main/kotlin/jhkim105/tutorials/jpadatetime/service/DbMetadataService.kt`
- [ ] T014 [US1] Implement `TimeRecordService` handling creation, retrieval, range search, and raw comparison mapping in `persistence/spring-jpa-datetime/src/main/kotlin/jhkim105/tutorials/jpadatetime/service/TimeRecordService.kt`

**Checkpoint**: User Story 1 (MVP) fully testable and operational with automated Testcontainers test suites.

---

## Phase 4: User Story 2 - REST API JSON Serialization & Deserialization (Priority: P2)

**Goal**: Expose REST API endpoints and verify ISO-8601 standard JSON serialization and deserialization across all time types.

**Independent Test**: Run `TimeRecordApiSpec` using MockMvc to test `POST /api/time-records`, `GET /api/time-records/{id}`, `GET /api/time-records/{id}/db-raw`, and `GET /api/time-records/search`.

### Tests for User Story 2

- [ ] T015 [P] [US2] Create MockMvc test spec verifying JSON serialization formats and endpoint contracts in `persistence/spring-jpa-datetime/src/test/kotlin/jhkim105/tutorials/jpadatetime/TimeRecordApiSpec.kt`

### Implementation for User Story 2

- [ ] T016 [US2] Implement `TimeRecordController` with endpoints `/api/time-records`, `/api/time-records/{id}`, `/api/time-records/{id}/db-raw`, `/api/time-records/search` in `persistence/spring-jpa-datetime/src/main/kotlin/jhkim105/tutorials/jpadatetime/controller/TimeRecordController.kt`

**Checkpoint**: User Story 2 REST API endpoints fully functional and tested against contract specifications.

---

## Phase 5: User Story 3 - Interactive Demo Web UI (Priority: P2)

**Goal**: Provide a responsive, browser-accessible demo UI to create time records and inspect JSON responses vs raw DB values side-by-side.

**Independent Test**: Start application and open `http://localhost:8080/index.html` to create records and verify dynamic table rendering.

### Implementation for User Story 3

- [ ] T017 [US3] Create single-page interactive demo UI with preset creation forms and comparison tables in `persistence/spring-jpa-datetime/src/main/resources/static/index.html`

**Checkpoint**: Web UI fully functional for interactive visual comparison.

---

## Phase 6: User Story 4 - Multi-DB Docker Compose Environment (Priority: P2)

**Goal**: Enable one-click launch of MySQL (3306), PostgreSQL (5432), app1 (8080, MySQL), and app2 (8081, Postgres) with external DB port access for GUI inspection.

**Independent Test**: Execute `docker compose up -d` and verify health/connectivity of both apps and DB ports.

### Implementation for User Story 4

- [ ] T018 [P] [US4] Create multi-stage container build file in `persistence/spring-jpa-datetime/Dockerfile`
- [ ] T019 [P] [US4] Create Docker Compose file orchestrating MySQL, PostgreSQL, app1 (8080), app2 (8081) in `persistence/spring-jpa-datetime/compose.yml`

**Checkpoint**: Multi-DB Docker environment ready for simultaneous comparison.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Documentation, comparative tables, and end-to-end verification.

- [ ] T020 [P] Create comprehensive reference documentation with MySQL vs PostgreSQL comparison summary in `persistence/spring-jpa-datetime/README.md`
- [ ] T021 Execute quickstart validation scenarios per `specs/20261009-jpa-datetime/quickstart.md` (`./gradlew test`)

---

## Dependencies & Execution Order

### Phase Dependencies

```
Phase 1: Setup
     ↓
Phase 2: Foundational (TimeRecord Entity, Repository, DTOs)
     ↓
Phase 3: User Story 1 (P1 - MVP: DB Persistence & Kotest Specs)
     ↓
Phase 4: User Story 2 (P2 - REST API & Controller)
     ↓
Phase 5: User Story 3 (P2 - Web UI) & Phase 6: User Story 4 (P2 - Docker Compose)
     ↓
Phase 7: Polish & Final Verification
```

### Parallel Opportunities

- **Phase 1**: T003, T004, T005, T006 can run in parallel.
- **Phase 2**: T010 can run in parallel with T008/T009.
- **Phase 3 (US1)**: T011 and T012 test specs can be written in parallel.
- **Phase 4 (US2)**: T015 test spec can be created before/alongside T016.
- **Phase 6 (US4)**: T018 (Dockerfile) and T019 (compose.yml) can be created in parallel.

---

## Parallel Example: User Story 1

```bash
# Launch test specs in parallel:
Task: "Create Kotest BehaviorSpec integration test with MySQL Testcontainer in persistence/spring-jpa-datetime/src/test/kotlin/jhkim105/tutorials/jpadatetime/TimeRecordPersistenceMysqlSpec.kt"
Task: "Create Kotest BehaviorSpec integration test with PostgreSQL Testcontainer in persistence/spring-jpa-datetime/src/test/kotlin/jhkim105/tutorials/jpadatetime/TimeRecordPersistencePostgresSpec.kt"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)
1. Complete Phase 1: Setup (T001 - T007)
2. Complete Phase 2: Foundational (T008 - T010)
3. Complete Phase 3: User Story 1 (T011 - T014)
4. **STOP and VALIDATE**: Run `./gradlew test` to verify MySQL & PostgreSQL persistence with Testcontainers.

### Incremental Delivery
1. Add User Story 2 (T015 - T016) → Verify REST endpoints and JSON serialization.
2. Add User Story 3 (T017) → Test interactive web UI in browser.
3. Add User Story 4 (T018 - T019) → Test `docker compose up -d` multi-DB simultaneous execution.
4. Polish (T020 - T021) → Documentation & final quickstart verification.
