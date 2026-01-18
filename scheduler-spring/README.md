# Spring Scheduler Tutorials

Spring Boot와 Kotlin을 사용한 스케줄러 구현 예제 프로젝트입니다.  
동일한 요구사항(헥사고널 아키텍처, 엔드포인트, DB 스키마)을 만족하지만, **스케줄링 실행 방식**이 서로 다른 두 가지 구현을 제공합니다.

## 프로젝트 구조 (Structure)

| 디렉토리 | 설명 |
| --- | --- |
| `scheduler-spring-polling` | **Polling 방식** 구현체 |
| `scheduler-spring-taskscheduler` | **Dynamic Scheduling 방식** 구현체 (Spring TaskScheduler) |
| `frontend` | 두 백엔드 프로젝트가 공통으로 사용하는 React Admin UI |
| `http` | API 테스트를 위한 HTTP 요청 예시 파일 (.http) |

## 구현 방식 비교 (Comparison)

두 프로젝트의 핵심 차이점은 **"스케줄을 어떻게 실행하는가?"** 입니다.

| 특징 | Polling 방식 (`scheduler-spring-polling`) | Dynamic Scheduling 방식 (`scheduler-spring-taskscheduler`) |
| :--- | :--- | :--- |
| **작동 원리** | 주기적(예: 1초)으로 DB를 조회(`polling`)하여 실행 시간이 된 작업을 찾습니다. | 스케줄 등록 시점에 메모리(`TaskScheduler`)에 타이머를 설정하여 정확한 시간에 실행합니다. |
| **장점** | • **단순함**: 구현이 직관적입니다.<br>• **복구 용이성**: 서버가 다운되어도 다음 폴링 때 놓친 작업을 찾기 쉽습니다. | • **실시간성**: 폴링 주기에 따른 지연 없이 정시에 즉시 실행됩니다.<br>• **효율성**: 할 일이 없을 때 불필요한 DB 조회를 하지 않습니다. |
| **단점** | • **지연**: 최대 폴링 주기만큼 실행이 늦어질 수 있습니다.<br>• **부하**: 작업이 없어도 계속 DB를 조회(Short Polling)합니다. | • **복잡성**: 서버 시작 시 DB 스케줄을 메모리로 로딩(`Loader`)해야 동기화가 됩니다.<br>• **관리**: 메모리와 DB 상태 불일치 가능성을 관리해야 합니다. |
| **클러스터링** | DB Lock을 사용하여 여러 서버 중 하나만 실행 (안전함). | 실행 시점에 DB Lock(`tryLockSchedule`)을 획득하여 중복 실행 방지. |
| **주요 코드** | `@Scheduled(fixedDelay = ...)` | `taskScheduler.schedule(runnable, trigger)` |

## 공통 사항 (Common)
- **Architecture**: 헥사고널 아키텍처 (Hexagonal Architecture)
- **Tech Stack**: Kotlin, Spring Boot 3.5+, MariaDB, JPA
- **Frontend**: Vite + React + TypeScript (공유)

## 실행 방법 (How to Run)

각 프로젝트 디렉토리로 이동하여 실행합니다.

```bash
# Polling 방식 실행
cd scheduler-spring-polling
./gradlew bootRun

# 또는

# Dynamic 방식 실행
cd scheduler-spring-taskscheduler
./gradlew bootRun
```

프론트엔드는 별도로 실행합니다.

```bash
cd frontend
npm install
npm run dev
```
