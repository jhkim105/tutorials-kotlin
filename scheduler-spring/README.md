# Scheduler Spring

Dynamic scheduler system with a Kotlin Spring Boot backend and a React admin UI.

## Structure
- `src/`: Spring Boot 3.5.x (Kotlin), hexagonal architecture, MariaDB
- `http/`: API request examples
- `frontend/`: Vite + React + TypeScript admin UI

## Backend

### Prerequisites
- JDK 17+
- MariaDB (Docker OK)

### Configure
- `src/main/resources/application.yml`
  - `spring.datasource.*`
  - `spring.jpa.properties.hibernate.jdbc.time_zone`
  - scheduler intervals under `scheduler.*`

### Run
```bash
./gradlew bootRun
```

### API
- Schedules (cursor pagination)
  - `GET /api/schedules?cursor=&limit=`
- Executions (offset pagination)
  - `GET /api/executions?limit=&offset=`
- Manual execution
  - `POST /api/executions/manual`
- Tasks
  - `GET /api/tasks`

HTTP examples are in `http/`.

## Frontend

### Configure
- Copy `.env.example` to `.env` and set `VITE_API_BASE_URL` if needed.

### Run
```bash
cd frontend
npm install
npm run dev
```

## Notes
- Scheduling uses Spring's `@Scheduled` polling with DB locks for clustered execution.
- Cron expressions follow Spring's format (6 fields, seconds included).
- Sample tasks are registered under `src/main/kotlin/com/example/scheduler/adapters/out/task/`.
