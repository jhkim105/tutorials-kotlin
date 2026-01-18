# Scheduler Spring Polling

Spring Boot(Kotlin)와 React Admin UI를 사용한 동적 스케줄러 시스템입니다. **Polling(폴링)** 방식을 사용하여 일정 주기마다 DB를 확인하고 스케줄을 실행합니다.

## 구조 (Structure)
- `src/`: Spring Boot 3.5.x (Kotlin), 헥사고널 아키텍처, MariaDB
- `http/`: API 요청 예시 파일
- `../frontend/`: 상위 디렉토리에 위치한 통합 Vite + React + TypeScript Admin UI

## 백엔드 (Backend)

### 필수 조건 (Prerequisites)
- JDK 17+
- MariaDB (Docker 가능)

### 설정 (Configure)
- `src/main/resources/application.yml`
  - `spring.datasource.*`: 데이터베이스 연결 설정
  - `spring.jpa.properties.hibernate.jdbc.time_zone`: 타임존 설정
  - `scheduler.*`: 스케줄러 폴링 주기 설정

### 실행 (Run)
```bash
./gradlew bootRun
```

### API
- 스케줄 (커서 페이징)
  - `GET /api/schedules?cursor=&limit=`
- 실행 이력 (오프셋 페이징)
  - `GET /api/executions?limit=&offset=`
- 수동 실행
  - `POST /api/executions/manual`
- 태스크 목록
  - `GET /api/tasks`

HTTP 요청 예시는 상위 디렉토리의 `../http/`를 참고하세요.

## 프론트엔드 (Frontend)

프론트엔드 코드는 상위 디렉토리(`../frontend`)에 위치하며, 두 백엔드 프로젝트가 공유합니다.

### 설정 (Configure)
- `.env.example`을 `.env`로 복사하고, `VITE_API_BASE_URL`을 설정하세요 (필요한 경우).

### 실행 (Run)
```bash
cd ../frontend
npm install
npm run dev
```

## 참고 사항 (Notes)
- **Polling 방식**: Spring의 `@Scheduled`를 사용하여 일정 주기마다 DB를 폴링하고, DB Lock을 사용하여 클러스터 환경에서도 안전하게 실행합니다.
- Cron 표현식은 Spring 형식을 따릅니다 (초 포함 6필드).
- 샘플 태스크는 `src/main/kotlin/com/example/scheduler/adapters/out/task/`에 등록되어 있습니다.
