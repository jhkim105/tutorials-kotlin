# ClickHouse 로컬 개발 환경 가이드 (Docker Compose)

이 디렉토리는 로컬 개발 및 테스트를 위해 ClickHouse 데이터베이스 서버를 Docker Compose를 통해 신속하게 구동하고 접속할 수 있는 환경을 제공합니다.

---

## 1. ClickHouse 컨테이너 기동 및 중지

터미널에서 이 디렉토리(`clickhouse/docker-clickhouse`)로 이동한 후 실행합니다.

* **백그라운드 실행:**
  ```bash
  docker compose up -d
  ```
* **구동 상태 확인 (Health Check):**
  ```bash
  docker compose ps
  ```
* **컨테이너 중지:**
  ```bash
  docker compose down
  ```

---

## 2. ClickHouse 접속 정보 (3가지) 🔐

보안 등급 준수 및 안정적인 포트 바인딩을 위해 패스워드가 `default`로 강제 지정되어 있습니다.

### 방법 1: 웹 브라우저 접속 (Play UI) - 가장 편리함 💡
별도의 툴 설치 없이 크롬이나 사파리 브라우저를 통해 편리하게 대화형 SQL을 수행할 수 있습니다.

* **접속 주소:** [http://localhost:8123/play](http://localhost:8123/play)
* **인증 정보:**
  * **User:** `default`
  * **Password:** `default`

### 방법 2: GUI 툴 접속 (DBeaver, DataGrip 등)
기존 DB 관리 도구를 사용해 커넥션을 연결할 수 있습니다.

* **드라이버 선택:** `ClickHouse`
* **접속 정보:**
  * **Host:** `localhost`
  * **Port:** `8123` (HTTP 기반 JDBC 포트)
  * **Database:** `default`
  * **Username:** `default`
  * **Password:** `default`

### 방법 3: Docker 내장 CLI 접속
컨테이너 내부에 설치되어 있는 `clickhouse-client`를 터미널에서 즉시 이용하는 방법입니다.

* **명령어:**
  ```bash
  docker exec -it clickhouse-tutorials clickhouse-client --password default
  ```

---

## 3. SQL 플레이그라운드 기초 명령어

Play UI나 CLI를 통해 수행해 볼 수 있는 기본적인 ClickHouse 대화형 SQL 예제입니다.

```sql
-- 1. 생성된 테이블 리스트 확인
SHOW TABLES;

-- 2. Flyway 마이그레이션 적용 이력 검증
SELECT * FROM flyway_schema_history;

-- 3. 이커머스 상품 조회 데이터 확인
SELECT * FROM product_view_events;

-- 4. OLAP 집계 (상품별 누적 조회수 상위 순위 계산)
SELECT product_id, count() as views
FROM product_view_events
GROUP BY product_id
ORDER BY views DESC;
```
