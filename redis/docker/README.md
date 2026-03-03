# docker

Redis Cluster 환경을 로컬에서 구성하기 위한 Docker 설정입니다.

## 구성 방식

두 가지 방식으로 Redis Cluster를 구성할 수 있습니다.

| 방식 | 디렉토리 | 설명 |
|------|----------|------|
| Predixy 프록시 | `docker-redis-cluster-predixy/` | Predixy 프록시를 통해 단일 엔드포인트로 Cluster 접근 |
| Grokzen 이미지 | `docker-redis-cluster-grokzen/` | grokzen/redis-cluster 이미지 사용 |

## docker-redis-cluster-predixy (권장)

### 구성

- **Redis 노드**: 6개 (redis-0 ~ redis-5, 포트 7000~7005)
  - 마스터 3개 + 레플리카 3개 (`--cluster-replicas 1`)
- **Predixy 프록시**: 단일 진입점 (포트 `7009`)
- **네트워크**: `redis-cluster` bridge 네트워크

### 실행

```bash
cd docker/docker-redis-cluster-predixy

# 클러스터 시작 (redis-cluster-init 컨테이너가 자동으로 클러스터 구성)
docker compose up -d

# 상태 확인
docker ps
redis-cli -p 7009 cluster info
```

### 클러스터 초기화 방식

```yaml
redis-cluster-init:
  entrypoint: >
    sh -c "
    sleep 5;
    echo 'yes' | redis-cli --cluster create
    redis-0:7000 redis-1:7001 redis-2:7002
    redis-3:7003 redis-4:7004 redis-5:7005
    --cluster-replicas 1"
```

컨테이너 시작 후 5초 대기 후 자동으로 `redis-cli --cluster create` 명령을 실행하여 클러스터를 구성합니다.

### 연결 정보

| 항목 | 값 |
|------|-----|
| Predixy 프록시 주소 | `localhost:7009` |
| 개별 Redis 노드 | `localhost:7000` ~ `localhost:7005` |

### 정리

```bash
docker compose down -v
```

## Predixy란?

[Predixy](https://github.com/joyieldInc/predixy)는 Redis Cluster 앞단에 위치하는 고성능 프록시입니다.

- 클라이언트는 단일 주소(`localhost:7009`)로 연결
- Predixy가 내부적으로 슬롯 기반 라우팅 처리
- 클러스터를 지원하지 않는 클라이언트도 사용 가능

## 주요 학습 포인트

- Redis Cluster 구성 원리 (16384 슬롯, 마스터/레플리카 분배)
- `redis-cli --cluster create` 명령으로 클러스터 형성
- Predixy 프록시를 통한 단일 엔드포인트 추상화
- Docker Compose `depends_on` + 초기화 컨테이너 패턴
