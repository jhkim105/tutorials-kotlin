# Scheduler Quartz

Dynamic scheduler system with a Kotlin Spring Boot backend and a React admin UI.

## Structure
- `backend/`: Spring Boot 3.5.x (Kotlin), hexagonal architecture, MariaDB
- `frontend/`: Vite + React + TypeScript admin UI

## Backend

### Prerequisites
- JDK 17+
- MariaDB (Docker OK)

### Configure
- `backend/src/main/resources/application.yml`
  - `spring.datasource.*`
  - `spring.jpa.properties.hibernate.jdbc.time_zone`
  - scheduler intervals under `scheduler.*`

### Run
```bash
cd backend
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

HTTP examples are in `backend/http/`.

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
- Schedules use `next_run_at` for efficient, exactly-once triggering.
- Sample tasks are registered under `backend/src/main/kotlin/com/example/scheduler/adapters/out/external/`.
