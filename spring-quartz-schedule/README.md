# Spring Quartz Schedule Demo

이 프로젝트는 Spring Boot와 Quartz Scheduler를 통합하여 예약된 작업을 실행하는 데모 애플리케이션입니다.
Baeldung의 [Spring Quartz Schedule](https://www.baeldung.com/spring-quartz-schedule) 가이드를 참조하여 Kotlin으로 작성되었습니다.

## 기술 스택

- **Spring Boot**: 3.5.9
- **Kotlin**: 1.9.25
- **Quartz Scheduler**: 작업 예약 및 실행
- **Spring Boot Actuator**: Quartz 작업 모니터링 및 관리

## 프로젝트 구조

- `src/main/kotlin/com/example/springquartz/job/SampleJob.kt`: 실제 실행될 Quartz Job 구현체입니다.
- `src/main/kotlin/com/example/springquartz/service/SampleJobService.kt`: Job에서 호출하는 비즈니스 로직 서비스입니다.
- `src/main/kotlin/com/example/springquartz/config/SchedulerConfig.kt`: JobDetail, Trigger, Scheduler를 설정하는 클래스입니다.
- `src/main/kotlin/com/example/springquartz/config/AutoWiringSpringBeanJobFactory.kt`: Quartz Job 인스턴스에 Spring Bean을 주입하기 위한 팩토리 클래스입니다.

## 실행 방법

1. 프로젝트지 디렉토리에서 터미널을 엽니다.
2. 다음 명령어로 애플리케이션을 실행합니다:
   ```bash
   ./gradlew bootRun
   ```
3. 애플리케이션이 시작되면 `SampleJob`이 1시간 간격(코드 설정상)으로 실행됩니다. 로그를 통해 확인할 수 있습니다.

## Actuator 엔드포인트

Quartz 작업을 모니터링하거나 관리하기 위해 Actuator 엔드포인트를 사용할 수 있습니다.

- **전체 Quartz 정보**: `http://localhost:8080/actuator/quartz`
- **특정 Job 트리거**: `POST http://localhost:8080/actuator/quartz/jobs/{groupName}/{jobName}`

예를 들어, `DEFAULT` 그룹의 `Qrtz_Job_Detail` 작업을 수동으로 즉시 실행하려면:

```bash
curl -X POST "http://localhost:8080/actuator/quartz/jobs/DEFAULT/Qrtz_Job_Detail" \
     -H "Content-Type: application/json" \
     -d '{"state":"running"}'
```

## 동적 스케줄링 API

실행 중에 작업을 동적으로 등록하거나 변경할 수 있는 API입니다.

### 1. 새로운 작업 등록
5초마다 실행되는 `new-job`이라는 이름의 작업을 등록합니다.

```bash
curl -X POST "http://localhost:8080/api/jobs/schedule?name=new-job&intervalSeconds=5"
```

### 2. 작업 스케줄 변경
위에서 등록한 `new-job`의 실행 주기를 10초로 변경합니다.

```bash
curl -X POST "http://localhost:8080/api/jobs/reschedule?name=new-job&intervalSeconds=10"
```
