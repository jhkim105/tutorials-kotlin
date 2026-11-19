# tutorials-kotlin Constitution

## Core Principles

### 1. Kotlin & Modern Spring Boot Standards
- **Kotlin Idioms**: Follow standard Kotlin conventions, minimize boilerplate, and avoid Java-style semicolons or redundant patterns.
- **Spring Boot 3.x**: Target modern Spring Boot 3.x and Java 17/21 as the baseline.
- **Gradle Kotlin DSL**: Maintain build configurations using `build.gradle.kts`.

### 2. Kotlin-friendly Logging
- Always use `kotlin-logging` (`io.github.oshai:kotlin-logging-jvm`) instead of direct SLF4J `LoggerFactory`.
- Standard logger initialization:
  ```kotlin
  import io.github.oshai.kotlinlogging.KotlinLogging

  private val logger = KotlinLogging.logger {}
  ```

### 3. BDD-Style Testing & Verification
- **Kotest & JUnit 5**: Prefer `kotest` (`BehaviorSpec` or `DescribeSpec`) with `io.kotest:kotest-runner-junit5` and `MockK` / `SpringMockk`.
- **Integration Tests**: Use Testcontainers for external dependencies (e.g. MariaDB, PostgreSQL, Redis, Kafka, ClickHouse, MongoDB) to ensure reproducible and standalone verification.
- Every PoC/demo must include clear executable tests validating the target hypothesis or technology behavior.

### 4. Modular PoC & Prototype Structure
- The repository is organized by topic subdirectories (e.g. `batch/`, `caching/`, `kafka/`, `persistence/`, `redis/`, `web/`).
- Each demo/subproject should be self-contained, runnable (`./gradlew bootRun` / `./gradlew test`), and easy to understand as a standalone reference.

### 5. Spec-Driven Knowledge Archiving
- When working on complex PoCs, clearly capture:
  1. **Objective / Hypothesis**: Why are we evaluating this technology or pattern?
  2. **Acceptance Criteria**: What behavior proves that this PoC succeeded?
  3. **Verification**: Executable tests or demo endpoints confirming the behavior.

## Governance & Maintenance
- All new modules and specifications generated through Spec Kit should adhere to the rules above.
- Amendments to this constitution can be made as the repository evolves or adopts newer Kotlin/Spring standards.

**Version**: 1.0.0 | **Ratified**: 2026-10-09
