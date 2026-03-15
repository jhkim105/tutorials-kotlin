# GEMINI.md - Kotlin Tutorials

## Project Overview
This repository is a comprehensive collection of Kotlin and Spring Boot tutorial projects. It is organized into various subdirectories based on specific technologies or architectural patterns (e.g., `batch/`, `caching/`, `kafka/`, `web/`). Each subdirectory is an independent or multi-module Gradle project.

### Main Technologies
- **Language:** Kotlin (1.9.x)
- **Framework:** Spring Boot (3.x)
- **Build Tool:** Gradle with Kotlin DSL (`build.gradle.kts`)
- **Persistence:** JPA, Spring Data JDBC, MongoDB, Redis, etc.
- **Testing:** JUnit 5, Kotest, MockK, SpringMockk, Testcontainers
- **Environment:** Java 17 / 21

## Project Structure
The repository is structured by topic:
- `architecture/`: Architectural patterns (e.g., Hexagonal Multi-module)
- `batch/`: Spring Batch examples
- `caching/`: Redis, Caffeine, dual-cache tutorials
- `concurrency/`: Coroutines, WebFlux, Virtual Threads
- `http-streaming/`: JSONL, SSE
- `io/`: File, JSON, Markdown processing
- `kafka/`: Spring Kafka, Kafka Streams, DLQ
- `kotlin-core/`: Pure Kotlin tutorials and ID generators
- `persistence/`: Spring JPA, Envers, Listeners
- `persistence-mongo/`: Spring Data MongoDB and transactions
- `redis/`: Redis Pub/Sub, Distributed Lock, Streams
- `scheduling/`: Spring Scheduler, Quartz
- `security/`: Spring Security, JWT, AuthZ
- `spring-core/`: AOP, Logging, Profiles, Retry, Resilience4j
- `testing/`: Kotest, Testcontainers
- `web/`: Spring MVC, WebFlux, Validation, HTTP Client
- `websocket/`: STOMP, WebSocket examples

## Building and Running
Since each subdirectory is an independent project, you must navigate into the specific project directory to run Gradle commands.

### Common Commands (inside a project directory)
- **Build:** `./gradlew build`
- **Run Application:** `./gradlew bootRun`
- **Run Tests:** `./gradlew test`
- **Clean:** `./gradlew clean`

## Development Conventions
- **Language:** Strictly use Kotlin.
- **Style:** Follow standard Kotlin/Spring Boot conventions.
- **Testing:** 
  - Use JUnit 5 as the primary test runner.
  - Use MockK or SpringMockk for mocking in Spring tests.
  - Kotest is used in some projects (check `testing/spring-kotest`).
- **Dependency Management:** Versions are typically managed within each project's `build.gradle.kts`.
- **Java Version:** Ensure you are using Java 17 or 21 as specified in the project's build file.

## Key Files to Reference
- `architecture/hexagonal-multi-module/`: For multi-module architecture examples.
- `testing/spring-kotest/`: For Kotest and Spring Boot integration.
- `web/spring-web/`: For basic Spring Boot web application structure.
