# Quartz vs Spring TaskScheduler Comparison

이 문서는 Spring Boot 환경에서 사용할 수 있는 두 가지 주요 스케줄링 방식인 **Quartz**와 **Spring TaskScheduler**를 비교 분석합니다.

## 1. 개요 (Overview)

### Spring TaskScheduler
Spring Framework에 내장된 기본적인 스케줄링 추상화 인터페이스입니다. `@Scheduled` 어노테이션이나 `TaskScheduler` 빈을 통해 간단하게 사용 가능하며, 별도의 의존성 없이 가볍게 동작합니다.

### Quartz Scheduler
다양한 기능을 제공하는 강력한 오픈소스 잡 스케줄링 라이브러리입니다. 복잡한 스케줄링, 클러스터링, 영속성(Persistence), 실패 처리(Misfire handling) 등을 기본적으로 지원합니다.

---

## 2. 상세 비교 (Comparison Table)

| 아키텍처 / 기능 | Spring TaskScheduler | Quartz Scheduler |
| :--- | :--- | :--- |
| **복잡도** | 낮음 (설정 간편) | 중간 ~ 높음 (Job, Trigger, Scheduler 개념 필요) |
| **클러스터링 (Clustering)** | **미지원** (기본적으로 단일 노드용) | **강력 지원** (DB 기반 락킹으로 중복 실행 방지) |
| **분산 환경 동기화** | `ShedLock` 같은 별도 라이브러리 필요 | 네이티브 지원 (JDBC JobStore 사용 시) |
| **스케줄 저장소 (Persistence)** | **메모리 (In-Memory)** | **DB (JDBC)** 또는 메모리 (RAM) |
| **서버 재시작 시 상태** | 초기화됨 (스케줄 유실 가능성) | DB에 남아있어 복구/재실행 가능 |
| **Misfire (놓친 작업) 처리** | 제한적 | 다양한 정책 제공 (즉시 실행, 다음 주기 실행 등) |
| **동적 스케줄링** | 가능하지만 관리가 번거로움 (`ScheduledFuture` 직접 관리) | JobKey/TriggerKey를 통해 체계적으로 CRUD 가능 |

---

## 3. 심층 분석 (Deep Dive)

### 3.1 Spring TaskScheduler
**장점**:
*   설정이 매우 간단합니다 (`@EnableScheduling` 하나면 끝).
*   가벼운 주기적 작업(예: 캐시 갱신, 로그 정리)에 적합합니다.
*   외부 DB 의존성이 없습니다.

**단점**:
*   **상태 비저장(Stateless)**: 서버가 재시작되면 등록된 동적 스케줄이 모두 사라집니다.
*   **스케일 아웃 어려움**: 서버를 2대 이상 띄우면 작업이 **중복 실행**됩니다. 이를 막으려면 개발자가 별도로 분산 락(Distributed Lock)을 구현해야 합니다.

### 3.2 Quartz Scheduler
**장점**:
*   **Stateful**: 모든 Job과 Trigger 정보를 DB에 저장하므로 서버가 죽어도 스케줄이 유지됩니다.
*   **Clustering**: 여러 서버가 떠 있어도 DB 락을 통해 **"정확히 한번(Exactly-once)"** 실행을 보장합니다. 로드 밸런싱 효과도 있습니다.
*   **유연성**: Cron 표현식 외에도 Calendar(공휴일 제외), 리스너, 플러그인 등 강력한 기능을 제공합니다.

**단점**:
*   초기 설정이 다소 복잡합니다 (테이블 생성, 프로퍼티 설정 등).
*   DB 부하가 발생할 수 있습니다 (짧은 주기의 폴링).

---

## 4. 추천 (Recommendation)

### Case A: 단순 반복 작업 / 단일 서버
> **추천: Spring TaskScheduler**
*   단일 인스턴스 서버이거나, 중복 실행되어도 상관없는 작업(예: 로컬 캐시 비우기).
*   스케줄이 런타임에 동적으로 거의 바뀌지 않는 경우.

### Case B: 핵심 비즈니스 로직 / 분산 환경 (현재 프로젝트)
> **추천: Quartz Scheduler**
*   **콘텐츠 딜리버리 시스템**과 같이 중복 발행이 치명적인 경우.
*   서버를 여러 대로 확장(Scale-out)할 계획이 있는 경우.
*   스케줄을 동적으로 등록/수정/삭제해야 하며, 서버 재시작 후에도 스케줄이 보존되어야 하는 경우.

## 5. 프로젝트 적용 가이드
현재 프로젝트는 헥사고널 아키텍처를 따르고 있어 `infra` 레이어만 교체/확장하면 됩니다.

*   **TaskScheduler 유지 시**: `ShedLock` 도입 필수, `ScheduledFuture` 관리 로직 보완 필요.
*   **Quartz 사용 시**: `application.yml`에서 `isClustered: true` 설정 및 외부 DB(MySQL 등) 연결 필요.
