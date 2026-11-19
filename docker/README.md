# Tutorials Kotlin - 통합 오픈소스 인프라 환경

이 디렉토리는 `tutorials-kotlin` 프로젝트의 각 모듈에서 사용하는 오픈소스 인프라(MySQL, PostgreSQL, Redis, Kafka, MongoDB, ClickHouse, Prometheus, Grafana 등)를 중앙에서 통합 관리하기 위한 Docker 환경입니다.

Docker Compose의 최신 모던 표준인 **`include` 지시문**과 **`profiles`**를 조합하여, 파일별 독립성을 유지하면서도 원하는 인프라 그룹만 유연하게 구동할 수 있습니다.

---

## 1. 디렉토리 구조

```text
docker/
├── docker-compose.yml              # 최상위 진입점 (include 지시문으로 하위 모듈 연동)
├── .env                            # 기본 실행 프로파일 설정 (기본값: COMPOSE_PROFILES=basic)
├── README.md                       # 본 안내 문서
├── tip/                            # 고아 컨테이너 역추적 & 정리 가이드
│
├── mysql/                          # [basic] MySQL 8.0 단독 (포트: 3306)
├── redis/                          # [basic] Redis 단독 (포트: 6379, redis.conf 마운트)
├── kafka/                          # [basic] Kafka 3.5 (9092) + Kafka-UI (8989)
├── mongodb/                        # [basic] MongoDB (27017) + Mongo Express (8081)
├── monitoring/                     # [monitoring] Prometheus (9090) + Grafana (3000)
├── postgresql/                     # [all] PostgreSQL 16 (포트: 5432)
├── clickhouse/                     # [all] ClickHouse 24.3 (포트: 8123, 9000)
│
├── mysql-replication/              # [특수] MySQL Master-Slave 복제 (포트: 3306, 3307, 3308)
├── redis-cluster/                  # [특수] Redis 6노드 클러스터 + Predixy (포트: 7000~7009)
└── mongodb-cluster/                # [특수] MongoDB 3노드 ReplicaSet (포트: 27017~27019)
```

---

## 2. 인프라 서비스 및 포트 요약

### 1) 표준 인프라 (Standalone)

| 프로파일 | 서비스명 | 컨테이너 이름 | 호스트 포트 | 내부 포트 | 기본 계정 / DB |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `basic`, `all` | `mysql` | `tutorials-mysql` | `3306` | `3306` | `root` / `root` (DB: `tutorial`) |
| `basic`, `all` | `redis` | `tutorials-redis` | `6379` | `6379` | `redis.conf` 적용 |
| `basic`, `all` | `kafka` | `tutorials-kafka` | `9092` | `9092` | KRaft 모드 브로커 |
| `basic`, `all` | `kafka-ui` | `tutorials-kafka-ui` | `8989` | `8080` | `admin` / `admin` |
| `basic`, `all` | `mongo` | `tutorials-mongo` | `27017` | `27017` | `root` / `pass1234` |
| `basic`, `all` | `mongo-express`| `tutorials-mongo-express`| `8081` | `8081` | `admin` / `pass1234` |
| `monitoring`, `all` | `prometheus` | `monitoring-prometheus` | `9090` | `9090` | Actuator 메트릭 수집 |
| `monitoring`, `all` | `grafana` | `monitoring-grafana` | `3000` | `3000` | `admin` / `admin` (SSE 대시보드 내장) |
| `all` | `postgres` | `tutorials-postgres` | `5432` | `5432` | `postgres` / `password` (DB: `tutorial`) |
| `all` | `clickhouse` | `tutorials-clickhouse` | `8123`, `9000` | `8123`, `9000` | `default` / `default` (DB: `default`) |

### 2) 특수 클러스터 / 복제 환경 (독립 실행)

> [!WARNING]
> 특수 클러스터는 기본 단독 인스턴스와 동일한 포트(`3306`, `6379`, `27017`)를 사용하므로, **단독 인스턴스를 중지한 후 해당 폴더에서 실행**해야 합니다.

| 환경 | 디렉토리 | 포트 | 설명 |
| :--- | :--- | :--- | :--- |
| **MySQL Replication** | `mysql-replication/` | `3306` (Master), `3307` (Slave1), `3308` (Slave2) | Master-Slave 복제 및 라우팅 실습용 |
| **Redis Cluster** | `redis-cluster/` | `7000~7005` (Redis 6노드), `7009` (Predixy 프록시) | Predixy 기반 Redis 클러스터 |
| **MongoDB Cluster** | `mongodb-cluster/` | `27017~27019` | 3노드 ReplicaSet 구성 |

---

## 3. 사용 방법

> 모든 명령어는 `tutorials-kotlin/docker` 디렉토리 기준으로 실행합니다.

### 1) 기본 인프라 실행 (MySQL, Redis, Kafka, MongoDB)
`docker/.env`에 기본값으로 `COMPOSE_PROFILES=basic`이 지정되어 있으므로, 옵션 없이 바로 실행할 수 있습니다.
```bash
cd docker
docker compose up -d
```
*(또는 명시적으로: `docker compose --profile basic up -d`)*

---

### 2) 모니터링 스택 실행 (Prometheus, Grafana)
```bash
docker compose --profile monitoring up -d
```
- **Prometheus Web UI**: http://localhost:9090
- **Grafana Web UI**: http://localhost:3000 (`admin` / `admin`)

---

### 3) 기본 인프라 + 모니터링 동시 실행
```bash
docker compose --profile basic --profile monitoring up -d
```

---

### 4) 전체 인프라 일괄 실행 (PostgreSQL, ClickHouse 포함)
```bash
docker compose --profile all up -d
```

---

### 5) 특정 단일 서비스만 콕 집어서 실행하는 팁
디렉토리를 이동할 필요 없이, 최상위 `docker/` 경로에서 서비스 이름만 지정하면 프로파일과 무관하게 해당 컨테이너만 즉시 실행됩니다.

```bash
# Redis만 단독 실행
docker compose up -d redis

# Kafka + Kafka UI만 실행
docker compose up -d kafka kafka-ui

# MySQL만 단독 실행
docker compose up -d mysql
```

---

### 6) 인프라 종료 및 볼륨 정리
```bash
# 기본 인프라 종료
docker compose down -v

# 모니터링 스택 종료
docker compose --profile monitoring down -v

# 전체 인프라 완전 종료
docker compose --profile all down -v
```

---

### 7) 특수 클러스터 실습 시 실행법
```bash
# 1. 포트 충돌 방지를 위해 기존 단독 인스턴스 종료
cd docker
docker compose down -v

# 2. 원하는 클러스터 디렉토리로 이동하여 실행
cd mysql-replication && docker compose up -d
# 또는
cd redis-cluster && docker compose up -d
# 또는
cd mongodb-cluster && docker compose up -d
```

---

## 4. 트러블슈팅 및 팁
- Compose 파일 위치를 분실한 고아 컨테이너 역추적 및 일괄 정리 명령어는 [tip/README.md](tip/README.md) 문서를 참고하세요.
