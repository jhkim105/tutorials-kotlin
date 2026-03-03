# docker-redis

Redis 단일 서버를 Docker Compose로 실행하는 환경입니다.

## 파일 구성

```
docker-redis/
├── docker-compose.yml  # Redis 컨테이너 설정
└── redis.conf          # Redis 서버 설정 (AOF, RDB, 바인드 등)
```

## 실행 방법

```bash
# 시작
docker compose up -d

# 상태 확인
docker compose ps

# 로그 확인
docker compose logs -f redis

# 종료 (데이터 유지)
docker compose down

# 종료 + 데이터 삭제
docker compose down -v
```

## 접속 확인

```bash
# 로컬 redis-cli
redis-cli ping        # → PONG
redis-cli info server # 서버 정보 확인

# 컨테이너 내부 접속
docker exec -it redis redis-cli
```

## 연결 정보

| 항목 | 값 |
|------|----|
| Host | `localhost` |
| Port | `6379` |
| Password | 없음 (기본) |

## 주요 설정 (redis.conf)

| 설정 | 기본값 | 설명 |
|------|--------|------|
| `bind` | `0.0.0.0` | 모든 인터페이스 허용 |
| `appendonly` | `yes` | AOF 영속성 활성화 |
| `save` | `900 1 / 300 10 / 60 10000` | RDB 스냅샷 조건 |
| `requirepass` | 비활성화 | 패스워드 필요 시 주석 해제 |
| `maxmemory` | 비활성화 | 메모리 제한 필요 시 주석 해제 |

## 데이터 영속성

- **AOF** (`appendonly.aof`): 모든 쓰기 명령을 로그로 저장 → 데이터 유실 최소화
- **RDB** (스냅샷): 일정 조건 충족 시 덤프 파일 생성 → 빠른 복구
- **Volume**: `redis-data` named volume으로 컨테이너 재시작 시에도 데이터 유지
