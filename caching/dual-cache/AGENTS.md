# Repository Guidelines

## Project Structure & Module Organization
- `src/main/kotlin/jhkim105/dualcache` houses the Spring Boot entry point `DualCacheApplication.kt` and cache orchestration logic; group new features in cohesive packages under this root.
- `src/main/resources` stores `application.properties` plus Redis/cache overrides; add profile-specific variants as `application-{profile}.properties`.
- `src/test/kotlin` mirrors the main packages; co-locate fixtures with the code they verify. Gradle outputs land in `build/` and stay untracked.

## Build, Test, and Development Commands
- `./gradlew build` compiles Kotlin sources, runs the full test suite, and produces the bootable jar under `build/libs/`.
- `./gradlew test` executes the JUnit 5 suites; append `-i` or `--tests '*Cache*'` to trace a failing scenario.
- `./gradlew bootRun` launches the local service using the Redis endpoints declared in `application.properties`.
- `./gradlew clean` clears generated outputs—run it before reproducing CI-only cache issues.

## Coding Style & Naming Conventions
- Use 4-space indentation, idiomatic `val` defaults, and expression-bodied functions when they improve readability.
- Classes stay in `PascalCase`, functions and properties in `camelCase`, and constants in `UPPER_SNAKE_CASE`.
- Keep all code under the `jhkim105.dualcache` package; align filenames with their primary class (e.g., `CacheWarmService.kt`).

## Testing Guidelines
- Tests rely on `spring-boot-starter-test` plus `kotlin-test-junit5`; name files `*Tests.kt` and functions `should...` for clarity.
- Mock external Redis calls in unit tests; reserve embedded Redis or Testcontainers checks for tagged integration suites.
- Target >80% line coverage on new cache flows and include hit/miss regression cases before opening a PR.

## Commit & Pull Request Guidelines
- Follow the concise, imperative format seen in history (`envelopmessage json`, `distinctBy`); keep subjects under ~50 characters.
- Pull requests should describe the cache scenario addressed, list manual verification steps, and link tracking issues when available.
- Include screenshots or `curl` transcripts for HTTP changes and call out any new configuration keys added to `application.properties`.

## Security & Configuration Tips
- Never commit Redis credentials; inject them via environment variables or secrets managers and document required keys in the PR.
- Validate dual-cache fallbacks by toggling `spring.data.redis.*` properties locally before pushing to avoid production regressions.
