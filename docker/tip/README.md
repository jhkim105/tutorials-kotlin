# Docker Compose 팁 & 고아 인스턴스 정리 가이드

오픈소스 인프라 관리 시 `docker-compose.yml` 위치를 분실했거나, 개별 모듈에서 띄운 인스턴스를 일괄 추적/삭제하는 방법 및 인프라 통합 가이드입니다.

---

## 1. `docker-compose.yml` 위치를 모를 때 삭제/추적하기

Docker Compose로 실행된 컨테이너는 Docker Label에 **실행 파일 경로**와 **프로젝트 이름**이 자동으로 기록됩니다.

### 방법 1. 원본 `docker-compose.yml` 위치 역추적 (추천)
```bash
# 실행 중인 컨테이너 확인
docker ps

# 원본 docker-compose.yml 절대 경로 확인
docker inspect -f '{{ index .Config.Labels "com.docker.compose.project.config_files" }}' <컨테이너명 또는 ID>

# 또는 작업 디렉토리 경로 확인
docker inspect -f '{{ index .Config.Labels "com.docker.compose.project.working_dir" }}' <컨테이너명 또는 ID>
```
경로를 확인한 후 해당 디렉토리로 이동하여 `docker compose down -v`를 실행합니다.

---

### 방법 2. 프로젝트 이름(`-p`)으로 위치 무관하게 정상 종료
Compose 파일 경로를 몰라도 **프로젝트 이름**만 알면 볼륨/네트워크까지 깔끔하게 제거할 수 있습니다.

```bash
# 1. 프로젝트 이름 확인
docker inspect -f '{{ index .Config.Labels "com.docker.compose.project" }}' <컨테이너명>

# 2. 프로젝트 이름으로 완전히 종료 및 볼륨 삭제
docker compose -p <프로젝트이름> down -v
```

---

### 방법 3. Docker CLI로 강제 일괄 정리
```bash
# 1. 특정 컨테이너 강제 삭제
docker rm -f <컨테이너ID>

# 2. Docker Compose로 띄워진 모든 컨테이너 일괄 강제 삭제
docker rm -f $(docker ps -aq --filter "label=com.docker.compose.project")

# 3. 미사용 볼륨 및 네트워크 정리
docker volume prune -f
docker network prune -f
```

---

## 2. 공통 인프라 통합 관리 (Compose Profiles 권장)

개별 하위 모듈마다 중복된 docker-compose(MySQL, Redis, Kafka 등)를 선언하면 포트 충돌 및 리소스 낭비가 발생합니다. 최상위 `docker/` 디렉토리에 모아서 관리하는 것을 권장합니다.

### 추천 패턴: `profiles` 활용

```yaml
# docker/docker-compose.yml 예시
services:
  mysql:
    image: mysql:8.0
    profiles: ["mysql", "all"]
    ports: ["3306:3306"]
    ...

  redis:
    image: redis:7-alpine
    profiles: ["redis", "all"]
    ports: ["6379:6379"]
    ...

  kafka:
    image: confluentinc/cp-kafka:...
    profiles: ["kafka", "all"]
    ...
```

- **특정 인프라만 실행**: `docker compose --profile redis up -d`
- **전체 인프라 실행**: `docker compose --profile all up -d`
- **종료 및 리소스 회수**: `docker compose --profile all down -v`
