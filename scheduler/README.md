# Scheduler Demo: Quartz vs TaskScheduler

이 프로젝트는 두 가지 스케줄링 방식(Quartz, Spring TaskScheduler)을 비교하기 위한 데모입니다.
멀티모듈 구조로 core(domain+application)를 공유하고, infra 및 API는 방식별로 분리합니다.

## 구성
- scheduler-core: 도메인 모델/규칙 + 유스케이스/포트
- infra/scheduler-infra-persistence: 공용 JPA 영속성 어댑터
- infra/scheduler-infra-actions: Action 외부 의존 어댑터
- scheduler-task/adapters/task-adapters: TaskScheduler API + 스케줄러 어댑터
- scheduler-quartz/adapters/quartz-adapters: Quartz API + 스케줄러 어댑터
- scheduler-task/app/app-task: TaskScheduler 앱 실행 모듈
- scheduler-quartz/app/app-quartz: Quartz 앱 실행 모듈

## TaskScheduler 방식
### 개요
Spring이 제공하는 간단한 스케줄링 추상화(TaskScheduler, @Scheduled)를 사용합니다.
인메모리 트리거 기반이며, 애플리케이션 프로세스가 내려가면 스케줄 상태는 유지되지 않습니다.

### 장점
- 설정이 간단하고 러닝 커브가 낮음
- 메모리 기반이라 오버헤드가 적음
- 단일 인스턴스, 단순 스케줄에 적합

### 단점
- 스케줄 상태/실행 내역의 내구성 부족
- 분산/클러스터 환경에서 제어가 어려움
- 장애 복구/미스파이어 처리에 한계

## Quartz 방식
### 개요
Quartz Scheduler를 사용하며 JDBC JobStore로 스케줄을 영속화할 수 있습니다.
다양한 트리거 정책과 클러스터링 지원이 강점입니다.

### 장점
- 스케줄/트리거 상태의 영속성 제공
- 미스파이어, 재시도 등 고급 정책 지원
- 클러스터 환경에서 스케줄 분산/조정 가능

### 단점
- 설정과 운영 복잡도가 높음
- 테이블/스키마 관리 필요
- 단순 케이스에는 과도한 구성일 수 있음

## 언제 무엇을 쓰면 좋을까?
- TaskScheduler: 단일 인스턴스, 간단한 주기 작업, 빠른 도입이 필요한 경우
- Quartz: 스케줄 영속성/클러스터링/복잡한 트리거가 필요한 경우

## 실행
TaskScheduler 버전:

```
./gradlew :scheduler-task:app:app-task:bootRun
```

Quartz 버전:

```
./gradlew :scheduler-quartz:app:app-quartz:bootRun
```
