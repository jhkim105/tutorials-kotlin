# Slave Scale-Out 아키텍처 및 JDBC Load Balancing 방식 검토 리서치

본 문서는 Master-Slave (Write/Read Replica) 환경에서 Read Replica(Slave)를 유연하게 확장(Scale-Out)하기 위한 다양한 아키텍처 방식과 **JDBC Driver 레벨 로드밸런싱(`jdbc:mysql:loadbalance://...`)의 한계 및 Best Practice**를 정리한 리서치 문서입니다.

---

## 1. 검토 배경 및 질문

* **검토 질문**: 
  * "슬레이브를 유연하게 Scale-Out 하려면 고정된 Multi DataSource 빈 대신, 단일 Slave 설정에 여러 DB URL을 묶는 `jdbc:mysql:loadbalance://` 방식을 사용하는 것은 어떤가?"
  * "이 방식에 잠재적인 문제점은 없는지, 실무에서는 어떤 방식이 가장 권장되는가?"

---

## 2. JDBC Driver Load Balancing (`jdbc:mysql:loadbalance://...`) 분석

### 2.1 동작 방식 및 구성
MySQL Connector/J 드라이버는 단일 JDBC URL에 여러 슬레이브 호스트를 나열하여 드라이버 내부에서 라운드로빈 및 장애 노드 블랙리스트 처리를 지원합니다.

```yaml
datasource:
  master:
    url: jdbc:mysql://master-db:3306/mydb
  slave:
    # 단일 Slave 설정에 다중 Host 나열
    url: jdbc:mysql:loadbalance://slave1:3306,slave2:3306,slave3:3306/mydb?loadBalanceStrategy=roundRobin&loadBalanceBlacklistTimeout=5000
    pool-name: HikariPool-Slave
```

### 2.2 한계점 및 치명적인 문제점

#### ⚠️ 1) HikariCP 커넥션 풀과의 부조화 (실시간 쿼리 부하 쏠림)
* **근본 원인**: `jdbc:mysql:loadbalance`의 라운드로빈은 **"물리적 Connection을 새로 생성하는 시점(Connection Creation)"**에만 동작합니다.
* **문제 발생**:
  * Spring Boot 구동 시 HikariCP가 커넥션 풀을 미리 10~20개 생성합니다. 이때 드라이버는 각 슬레이브로 커넥션을 분배해 둡니다.
  * 하지만 실제 서비스 운영 중 들어오는 트랜잭션/쿼리 요청은 **이미 풀에 맺어진 커넥션을 꺼내 재사용**합니다.
  * 특정 스레드나 작업 패턴에 따라 반납된 유휴 커넥션만 반복해서 사용될 경우, **실제 쿼리 트래픽이 특정 슬레이브로만 몰리는 부하 불균형 현상**이 발생합니다.

#### ⚠️ 2) 동적 Scale-Out (슬레이브 증설) 트래픽 미반영
* 새로운 슬레이브(`slave4`)가 추가되었을 때:
  * URL 문자열을 수정해야 하므로 **애플리케이션 재기동**이 필요합니다.
  * JMX 등을 통해 런타임에 호스트를 추가하더라도, 기존 HikariCP 풀의 커넥션들은 이미 이전 슬레이브(`slave1~3`)에 연결되어 있어 커넥션이 만료(`maxLifetime`, 보통 30분)되어 재생성되기 전까지는 신규 슬레이브로 트래픽이 유입되지 않습니다.

#### ⚠️ 3) 슬레이브별 커넥션 풀 격리 및 모니터링 불가
* 모든 슬레이브가 1개의 HikariCP 풀을 공유하므로, 특정 슬레이브 인스턴스에 장애나 락(Lock), 쿼리 지연이 발생했을 때:
  * 해당 슬레이브로 향하는 풀만 격리하거나 서킷브레이커를 걸 수 없습니다.
  * Grafana / Micrometer / JMX에서 슬레이브별 `Active Connections` 지표를 개별적으로 관측할 수 없습니다.

---

## 3. Scale-Out 방식별 비교 분석

| 비교 항목 | ① JDBC Driver (`loadbalance://`) | ② Spring Routing DataSource (현재 모듈 방식) | ③ Cloud / Proxy 인프라 (업계 표준 Best Practice) |
| :--- | :--- | :--- | :--- |
| **분산 레벨** | JDBC 드라이버 (물리 커넥션 생성 시) | 애플리케이션 트랜잭션 레벨 (`@Transactional(readOnly)`) | AWS Aurora Reader Endpoint / ProxySQL / HAProxy |
| **트랜잭션 분산 정확도** | ❌ 낮음 (커넥션 재사용으로 쏠림 발생) | ⭕ 높음 (트랜잭션 단위로 정확히 라운드로빈) | ⭕ 높음 (L4/L7 프록시 기반 균등 분산) |
| **커넥션 풀 격리** | ❌ 불가 (단일 풀 공유) | ⭕ 가능 (슬레이브별 독립된 HikariCP 풀) | ⭕ 프록시 단일 엔드포인트 관리 |
| **Scale-Out 유연성** | ❌ 낮음 (URL 수정 및 재배포 필요) | 🔺 보통 (동적 DataSource 리로딩 구현 필요) | **🌟 최상 (앱 변경/재기동 전혀 없음)** |
| **장애 노드 감지/격리** | 🔺 드라이버 블랙리스트 의존 (지연 발생) | ⭕ 애플리케이션 레벨 헬스체크 및 격리 가능 | **🌟 최상 (인프라 레벨 자동 감지 및 Failover)** |

---

## 4. 실무 권장 아키텍처 가이드 (Best Practices)

### 1) 클라우드 매니지드 DB 환경 (AWS Aurora, GCP Cloud SQL) - 🌟 가장 권장
* **아키텍처**: `Master DataSource` + `Slave DataSource (Reader Cluster Endpoint)`
```text
Spring Boot (RoutingDataSource)
   ├── Master DataSource ──► AWS Aurora Writer Endpoint (단일 인스턴스)
   └── Slave DataSource  ──► AWS Aurora Reader Endpoint (다중 인스턴스)
                                     │ (AWS 내부 DNS / L4 라운드로빈)
                                     ├──► Read Replica 1
                                     ├──► Read Replica 2
                                     └──► Read Replica N (Auto-Scaling)
```
* **장점**: 슬레이브가 1대에서 10대로 Auto Scaling 되어도 애플리케이션 코드나 설정 변경 없이 완벽하게 자동 분산됩니다.

### 2) 온프레미스 / 자체 인프라 환경
* **권장안 A (Proxy 계층 도입)**:
  * 슬레이브 앞단에 **ProxySQL** 또는 **HAProxy**를 배치하여 단일 VIP/도메인을 애플리케이션의 Slave DataSource로 지정.
* **권장안 B (Dynamic Routing DataSource 패턴)**:
  * 현재 모듈의 `RoutingDataSource`를 기반으로 하되, Redis Pub/Sub, Spring Cloud Config, Consul 등을 통해 슬레이브 목록이 변경될 때 런타임에 `RoutingDataSource.setTargetDataSources()`를 호출하여 동적으로 HikariCP 풀을 교체하는 방식 적용.

---

## 5. 결론 요약

1. `jdbc:mysql:loadbalance://` 방식은 설정이 간단해 보이지만 **HikariCP 커넥션 풀링 환경에서 실시간 쿼리 분산 불균형 및 동적 Scale-Out 미반영**이라는 명확한 한계가 존재합니다.
2. 따라서 안정적인 고가용성/대용량 시스템에서는 **AWS Aurora Reader Endpoint / ProxySQL과 같은 인프라 엔드포인트 방식**을 사용하거나, **Spring `RoutingDataSource` 기반의 풀 격리 및 Dynamic DataSource 패턴**을 사용하는 것이 표준입니다.
