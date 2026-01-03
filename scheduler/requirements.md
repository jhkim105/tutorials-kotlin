## 데모 프로젝트 구성 요구사항 (API)

### 프로젝트 명(확정)
- scheduler-task-demo
- scheduler-quartz-demo

두 프로젝트는 서로 의존하지 않는 **완전 독립 Spring Boot Kotlin 애플리케이션**이다.

- Spring Boot 버전: 3 최신버전

---

## 목적
동적 스케줄링 아키텍처를 비교·검증하기 위해 두 가지 방식을 각각 별도의 데모 애플리케이션으로 구현한다.

- scheduler-task-demo  
  → Spring TaskScheduler 기반 동적 등록/취소 방식

- scheduler-quartz-demo  
  → Quartz + JDBC JobStore 기반 영속 스케줄링 방식

두 애플리케이션은 **동일한 도메인 모델과 실행 규칙**을 사용하며,
차이는 오직 “스케줄링 인프라 구현”에만 존재해야 한다.

---

## 프로토타입 범위 (1차)
- 스케줄 CRUD 및 enable/disable 제공
- 스케줄 실행 로그 기록(성공/실패/스킵)
- 단순 Action 3종만 우선 구현
- 애플리케이션 기동 시 샘플 스케줄 2~3개 자동 생성

---

## 공통 기능 요구사항 (두 앱 공통)

### 1. 스케줄 데이터 모델
#### delivery_schedule
- id: Long (PK)
- name: String
- scheduleType: CRON | ONCE
- cronExpression: String?        # scheduleType=CRON
- runAt: Instant?                # scheduleType=ONCE
- enabled: Boolean
- actionKey: String              # 실행할 기능 식별자
- payload: JSON/Text             # 실행 파라미터
- timezone: String?              # optional
- updatedAt: Instant

※ actionKey는 서버에 사전 등록된 Action Registry의 key와 반드시 일치해야 한다.

---

### 2. 기능 실행 모델 (Action Execution Model)

#### 기본 원칙
- 스케줄 정보에는 **실행 시점 정보 + 실행할 기능의 식별자(actionKey) + 파라미터(payload)** 만 저장한다.
- 실제 실행 로직은 서버 코드에 정의된 **Action Registry(화이트리스트)** 를 통해서만 수행한다.
- DB에 클래스명, 코드, 스크립트, 표현식을 저장하여 실행하는 방식은 허용하지 않는다.

#### Action Registry 요구사항
- 서버 시작 시 모든 실행 가능한 action을 사전 등록한다.
- actionKey → ActionHandler 매핑은 코드 레벨에서만 정의한다.
- 등록되지 않은 actionKey는 실행 시 오류로 처리한다.

#### 프로토타입 기본 actionKey
- PRINT_MESSAGE      # 콘솔 메시지 출력
- CREATE_FILE        # 지정 경로에 파일 생성
- HTTP_PING          # 지정 URL GET 호출(Mock 가능)

#### 확장 예시 actionKey
- CONTENT_PUSH
- EMAIL_DELIVERY
- SMS_DELIVERY
- INAPP_NOTIFICATION
- WEBHOOK_CALL

---

### 3. payload 처리 요구사항
- payload는 JSON 구조로 저장한다.
- ActionHandler는 자신의 payload 구조에 대해서만 해석 책임을 가진다.
- payload 스키마 변경은 action 단위로 관리한다.

---

### 4. 기능 실행 흐름 (공통)
1) 스케줄러는 실행 시점에 scheduleId만 전달한다.
2) 실행 시점에 scheduleId 기준으로 최신 delivery_schedule을 DB에서 조회한다.
3) actionKey를 Action Registry로 resolve 한다.
4) payload를 파싱하여 ActionHandler를 실행한다.
5) 실행 결과를 delivery_execution에 기록한다.

---

### 5. 실행 시 최신 스케줄 조회 전략
- 실행 시점에 scheduleId를 기준으로 DB에서 최신 delivery_schedule을 조회한다.
- Runnable / Quartz JobDataMap에는 scheduleId만 전달하는 방식을 기본으로 한다.
- 이를 통해 payload, enabled 상태 변경이 즉시 반영되도록 한다.

---

### 6. 멱등성 및 중복 실행 방지
#### delivery_execution
- id
- scheduleId
- fireTime
- status: SUCCESS | FAILED | SKIPPED
- errorMessage
- unique(scheduleId, fireTime)

- 동일한 scheduleId + fireTime 조합은 단 한 번만 실행된다.
- unique 제약조건 위반 시 해당 실행은 SKIPPED 처리한다.
- TaskScheduler / Quartz 모두 동일한 방식으로 적용한다.

---

### 7. REST API (최소 구현)
- POST /schedules           : 스케줄 생성
- PUT /schedules/{id}       : 스케줄 수정
- POST /schedules/{id}/enable
- POST /schedules/{id}/disable
- GET /schedules            : 목록 조회
- (선택) POST /schedules/{id}/trigger : 즉시 실행(디버깅용)

---

### 8. 데모 시나리오 (샘플 스케줄)
- 매 10초마다 PRINT_MESSAGE 실행 ("Hello Scheduler")
- 매 1분마다 CREATE_FILE 실행 (파일명: timestamp 기반)
- 30초마다 HTTP_PING 실행 (mock URL 가능)

---

## 프로젝트 구조 (모노레포)

root/
  README.md
  scheduler-task-demo/
    build.gradle.kts
    settings.gradle.kts
    src/main/kotlin/...
    src/main/resources/...
    src/test/kotlin/...
  scheduler-quartz-demo/
    build.gradle.kts
    settings.gradle.kts
    src/main/kotlin/...
    src/main/resources/...
    src/test/kotlin/...
  scheduler-admin-web/
    package.json
    src/...

각 프로젝트는:
- 자체적으로 실행 가능해야 한다.
- 기본 DB는 H2를 사용한다.
- 애플리케이션 실행 시 샘플 스케줄 데이터를 자동으로 2~3개 생성한다.

---

## scheduler-task-demo 요구사항

### 스케줄링 방식
- Spring TaskScheduler 사용
- 부팅 시 enabled 스케줄을 DB에서 모두 로드하여 동적으로 등록한다.
- scheduleId → ScheduledFuture 맵을 유지하여 cancel 가능하도록 한다.

### 스케줄 타입 처리
- CRON: CronTrigger 사용
- ONCE: Date.from(runAt) 사용

### 변경 반영 방식
- updatedAt 기준 주기적 폴링(예: 10초)으로 변경 사항을 감지하여 재등록/취소
- 또는 스케줄 저장 직후 즉시 재등록(선택)

---

## scheduler-quartz-demo 요구사항

### 스케줄링 방식
- spring-boot-starter-quartz 사용
- Quartz JDBC JobStore 설정
- 단일 인스턴스 기준으로 구현하되, clustered 설정 가능하도록 구성

### 스케줄 반영 방식
- CRON: upsert 방식으로 Job/Trigger 관리
- ONCE: 기존 Job 삭제 후 재등록
- DISABLE: Job 삭제

### 실행 처리
- Quartz Job 실행 시 scheduleId를 기준으로 DB에서 최신 스케줄 조회
- Action Registry를 통해 기능 실행

---

## 패키지 구조 가이드 (각 프로젝트 공통)

com.example.scheduler
  api/                # REST controllers (scheduler-*/adapters/api-*)
  domain/
    model/            # JPA entities
    action/           # ActionKey
  application/
    action/           # ActionRegistry, ActionHandler
    service/          # ScheduleService, ExecutionService
  scheduler-task/
    adapters/         # TaskScheduler 전용 구현
    app/              # TaskScheduler SpringBootApplication
  scheduler-quartz/
    adapters/         # Quartz 전용 구현
    app/              # Quartz SpringBootApplication
  infra/
    persistence/      # repositories
  bootstrap/          # SpringBootApplication, config, init data (scheduler-*/app)

---

## 구현 제약 요약
- 스케줄러(TaskScheduler / Quartz)는 “언제 실행할지”만 책임진다.
- “무엇을 실행할지”는 Action Registry를 통해서만 결정된다.
- 두 데모 간 도메인/실행 모델은 동일해야 하며, 스케줄링 구현만 다르다.

---

## 확장 가능성 (2차 이후)
기본 기능 구현 후, 다음의 “동적 Action 추가”를 확장 가능하도록 설계한다.

### 1. Webhook Action
- 관리자가 URL, HTTP method, headers, payload 템플릿을 등록한다.
- 실행 시 스케줄 payload를 템플릿에 바인딩하여 외부 시스템으로 전달한다.
- 실행 실패 시 재시도 정책은 기존 execution 정책을 따른다.

### 2. 템플릿 기반 Action
- 서버에 사전 정의된 템플릿 목록을 제공한다.
- 관리자는 템플릿을 선택하고 파라미터만 입력한다.
- 템플릿 추가/수정은 코드 변경으로만 수행한다.

### 설계 원칙
- 기본 Action Registry 구조를 유지한다.
- 동적으로 추가된 Action은 “실행 코드 삽입”이 아닌 “파라미터화된 실행”으로 제한한다.
- 보안 및 안정성을 위해 승인된 템플릿과 Webhook만 허용한다.
