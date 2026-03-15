# Quartz Scheduler 이해하기

Quartz는 Java 애플리케이션에 통합할 수 있는 오픈 소스 작업 예약 라이브러리입니다. 간단한 간격 실행부터 복잡한 일정(cron 표현식 지원)까지 다양한 스케줄링 요구사항을 처리할 수 있습니다.

## 주요 컴포넌트

Quartz는 모듈식 아키텍처를 가지며, 다음과 같은 주요 컴포넌트로 구성됩니다.

### 1. Job (작업)
실제로 수행해야 할 작업을 정의하는 인터페이스입니다. `execute(JobExecutionContext context)` 메서드 하나만을 가집니다.
개발자는 이 인터페이스를 구현하여 비즈니스 로직을 작성하거나, 서비스 클래스를 호출하도록 만듭니다.

### 2. JobDetail (작업 상세)
`Job` 인터페이스를 구현한 클래스의 인스턴스 그 자체가 아니라, 작업을 실행하는 데 필요한 상세 정보를 정의하는 객체입니다.
Quartz는 `JobDetail`을 통해 작업의 식별자(이름, 그룹), 설명, 작업 데이터(`JobDataMap`) 등을 관리합니다.
작업이 실행될 때마다 `JobDetail`을 기반으로 새로운 `Job` 인스턴스가 생성됩니다.

### 3. Trigger (트리거)
작업을 언제 실행할지 결정하는 메커니즘입니다. "방아쇠" 역할을 합니다.
가장 일반적으로 사용되는 두 가지 트리거 유형은 다음과 같습니다:
- **SimpleTrigger**: 특정 시간에 시작하여 일정 간격으로 반복 실행할 때 사용합니다. (예: 10초마다 실행)
- **CronTrigger**: 달력과 유사한 일정(Unix cron 표현식)을 기반으로 실행할 때 사용합니다. (예: 매주 금요일 오후 5시 실행)

### 4. Scheduler (스케줄러)
`Job`과 `Trigger`를 관리하고 실행하는 핵심 엔진입니다. 일종의 "관제탑" 역할을 수행합니다.
`Job`("무엇을")과 `Trigger`("언제")를 연결해줍니다.
`SchedulerFactory`를 통해 생성되며, `JobDetail`과 `Trigger`를 스케줄러에 등록하여 작업을 예약합니다.
스케줄러가 시작(`start()`)되면 등록된 트리거 조건에 따라 작업을 실행합니다.

또한, **실행 중에 동적으로 작업을 추가/수정/삭제**할 수 있는 API를 제공합니다.
예를 들어 `scheduler.scheduleJob(job, trigger)` 메서드를 호출하여 런타임에 새로운 예약을 걸거나, `scheduler.rescheduleJob(triggerKey, newTrigger)`로 실행 주기를 변경할 수 있습니다.

### 5. JobStore (작업 저장소)
작업과 트리거의 정보를 저장하는 메커니즘입니다.
- **RAMJobStore (In-Memory)**: 메모리에 정보를 저장합니다. 가장 빠르지만 애플리케이션이 종료되면 예약 정보가 사라집니다.
- **JDBCJobStore (Database)**: 데이터베이스에 정보를 저장합니다. 영속성을 보장하므로 애플리케이션 재시작 후에도 스케줄이 유지됩니다. 클러스터링 환경에서도 사용됩니다.

## Spring Boot와의 통합

Spring Boot는 `spring-boot-starter-quartz`를 통해 Quartz를 쉽게 사용할 수 있도록 지원합니다.
- **Auto-Configuration**: `Scheduler`, `JobFactory` 등을 자동으로 설정해줍니다.
- **Bean 방식 설정**: `JobDetail`, `Trigger` 등을 Spring Bean으로 등록하면 스케줄러가 자동으로 이를 감지하여 예약할 수 있습니다.
- **의존성 주입**: `SpringBeanJobFactory`를 사용하면 Job 클래스 내부에서 `@Autowired`를 통해 Spring 관리 빈(Service, Repository 등)을 주입받아 사용할 수 있습니다.
