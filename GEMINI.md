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
- `batch/`: Spring Batch examples (`spring-batch`, `spring-batch-beanio`, `spring-batch-hexagonal`)
- `caching/`: Redis, Caffeine, dual-cache tutorials (`dual-cache`, `spring-cache-caffeine`, `spring-cache-redis`)
- `concurrency/`: Coroutines, WebFlux, Virtual Threads (`spring-web-vt`, `spring-webflux-coroutines`)
- `docker/`: Docker Compose configurations (`docker-kafka`)
- `http-streaming/`: JSONL, SSE (`jsonl`, `sse`)
- `io/`: File, JSON, BeanIO, Image, Markdown processing (`beanio`, `files`, `image-modules`, `json`, `markdown`)
- `kafka/`: Spring Kafka, Kafka Streams, DLQ (`spring-kafka`, `spring-kafka-dlq`, `spring-kafka-dynamic`, `kafka-streams-vs-consumer`)
- `kotlin-core/`: Pure Kotlin tutorials (`core-kotlin`, `coroutines`, `id-generator`)
- `multi-project/`: Gradle multi-project build examples (`multi-build-logic`, `multi-buildSrc`)
- `persistence/`: Spring JPA, JDBC, Envers, Listeners (`spring-jpa`, `spring-jdbc`, `spring-jpa-envers`, `spring-jpa-listener`)
- `persistence-mongo/`: Spring Data MongoDB and transactions (`spring-data-mongodb`, `spring-data-mongodb-transaction`)
- `redis/`: Redis Pub/Sub, Distributed Lock, Streams (`spring-data-redis`, `spring-data-redis-pubsub`, `spring-data-redis-reactive`, `spring-data-redisson-pubsub`, `redis-distributed-lock`, `redis-streams`, `redis-streams-vs-pubsub`)
- `scheduling/`: Spring Scheduler, Quartz (`scheduler`, `scheduler-quartz`, `scheduler-spring`, `spring-quartz-schedule`)
- `security/`: Spring Security, JWT, AuthZ (`spring-security`, `spring-security-jwt`, `jwt`, `authz/authz-demo-api`, `authz/authz-spring-security-api`)
- `spring-cloud/`: Spring Cloud (`spring-cloud-aws`)
- `spring-core/`: AOP, Logging, Profiles, Properties, Retry, Resilience4j (`spring-aop`, `spring-boot-2`, `spring-logging`, `spring-profiles`, `spring-properties`, `spring-resilience4j`, `spring-retry`)
- `testing/`: Kotest, Testcontainers, Test Coverage (`spring-kotest`, `testcontainers`, `testcontainers-mariadb`, `test-coverage`)
- `web/`: Spring MVC, WebFlux, Validation, HTTP Client, REST Documentation (`spring-mvc`, `spring-mvc-validation`, `spring-web`, `spring-webflux`, `httpclient`, `rest-documentation`)
- `websocket/`: STOMP, WebSocket examples (`websocket`, `websocket-stomp`)

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
- **Logging:** Use `kotlin-logging` (`KotlinLogging`) for all logging. Do NOT use SLF4J `LoggerFactory` directly.
  ```kotlin
  import io.github.oshai.kotlinlogging.KotlinLogging
  
  private val logger = KotlinLogging.logger {}
  ```
- **Testing:** 
  - Use JUnit 5 as the primary test runner.
  - Use MockK or SpringMockk for mocking in Spring tests.
  - Kotest is used in some projects (check `testing/spring-kotest`).
- **Dependency Management:** Versions are typically managed within each project's `build.gradle.kts`.
- **Java Version:** Ensure you are using Java 17 or 21 as specified in the project's build file.

## Key Files to Reference
- `testing/spring-kotest/`: For Kotest and Spring Boot integration.
- `web/spring-web/`: For basic Spring Boot web application structure.
- `multi-project/`: For Gradle multi-project build configuration examples.
- `concurrency/`: For Coroutines and Virtual Threads usage.
