## 관리자 Web UI 요구사항 (React)

### 기술 스택
- React 기반 구현
- Vite + TypeScript 사용
- API Base URL은 환경 변수로 설정

---

## 핵심 기능
- 스케줄 등록/수정/삭제/조회 UI 제공
- 스케줄 enable/disable 토글 지원
- actionKey는 사전 등록된 목록에서만 선택 가능
- payload는 JSON 입력 텍스트로 관리
- 수동 실행(Manual Execution) 요청 UI 제공
- 실행 이력(Execution) 조회 UI 제공

---

## 화면 구성

### 1) 스케줄 목록 페이지
- 컬럼: id, name, scheduleType, cronExpression/runAt, enabled, actionKey, updatedAt
- 액션: 상세/수정, enable/disable, 삭제

### 2) 스케줄 등록/수정 폼
- 공통 입력: name, scheduleType, enabled, actionKey, payload
- scheduleType=CRON: cronExpression 입력
- scheduleType=ONCE: runAt 입력(ISO-8601)
- actionKey는 드롭다운(사전 정의 목록)
- payload는 JSON 텍스트 입력

### 3) 수동 실행 페이지
- 입력: actionKey(드롭다운), payload(JSON 텍스트)
- 실행 결과: executionId, status, createdAt

### 4) 실행 이력 페이지
- 기본 표시: executionId, taskId, executionType, status, attemptCount, createdAt
- 상세: scheduleId, payload, startedAt, completedAt, updatedAt

---

## API 연동 (Backend 기준)

### 공통
- Base URL: `VITE_API_BASE_URL`
- 날짜/시간: ISO-8601 문자열

### Task 목록
- `GET /api/tasks`
- 응답: `{ taskId, name, description }[]`
- 사용처: actionKey 드롭다운

### 스케줄
- `GET /api/schedules`
- `GET /api/schedules/{id}`
- `POST /api/schedules` (생성)
- `PUT /api/schedules/{id}` (수정)
- `PATCH /api/schedules/{id}/enable`
- `PATCH /api/schedules/{id}/disable`
- `DELETE /api/schedules/{id}`

요청 바디 (POST/PUT 공통):
```
{
  "name": "string",
  "scheduleType": "CRON|ONCE",
  "cronExpression": "string|null",
  "runAt": "2025-01-01T09:00:00Z|null",
  "enabled": true,
  "actionKey": "taskId",
  "payload": "string|null"
}
```

응답 바디:
```
{
  "id": "string",
  "name": "string",
  "scheduleType": "CRON|ONCE",
  "cronExpression": "string|null",
  "runAt": "string|null",
  "enabled": true,
  "actionKey": "taskId",
  "payload": "string|null",
  "nextRunAt": "string|null",
  "updatedAt": "string"
}
```

오류:
- 400: 잘못된 입력 (예: cronExpression 누락/형식 오류)
- 404: 스케줄 없음

### 수동 실행
- `POST /api/executions/manual`

요청 바디:
```
{
  "actionKey": "taskId",
  "payload": "string|null"
}
```

응답 바디:
```
{
  "executionId": "string",
  "scheduleId": "string|null",
  "taskId": "string",
  "executionType": "MANUAL",
  "status": "PENDING|RUNNING|SUCCESS|FAILED",
  "payload": "string|null",
  "attemptCount": 0,
  "createdAt": "string",
  "updatedAt": "string",
  "startedAt": "string|null",
  "completedAt": "string|null"
}
```

오류:
- 409: 동일 taskId 실행 중

### 실행 이력
- `GET /api/executions?limit=50`
- 응답: ExecutionResponse[] (위 동일)
