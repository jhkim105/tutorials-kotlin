#!/usr/bin/env bash

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$DIR"

echo "================================================================"
echo " Starting MySQL Master-Slave Environment (Docker Compose)"
echo "================================================================"

cleanup() {
    echo ""
    echo ">> [Cleanup] Stopping MySQL Docker containers..."
    docker compose down
    echo ">> MySQL containers stopped."
    exit 0
}

# Ctrl+C, 종료 시그널 수신 시 컨테이너 정상 정지
trap cleanup SIGINT SIGTERM

# 1. 포트 충돌 확인 및 기존 충돌 컨테이너 정지
echo ">> [1/2] Checking for conflicting Docker containers (ports 3306, 3307, 3308)..."
CONFLICTING_CONTAINERS=$(docker ps -q --filter "publish=3306" --filter "publish=3307" --filter "publish=3308")
if [ -n "$CONFLICTING_CONTAINERS" ]; then
    echo ">> Found conflicting containers. Stopping them..."
    docker stop $CONFLICTING_CONTAINERS
fi

OLD_CONTAINERS=$(docker ps -aq --filter "name=mysql-master" --filter "name=mysql-slave1" --filter "name=mysql-slave2")
if [ -n "$OLD_CONTAINERS" ]; then
    echo ">> Cleaning up existing routing datasource containers..."
    docker rm -f $OLD_CONTAINERS > /dev/null 2>&1 || true
fi

# 2. Docker Compose 기동
echo ">> [2/2] Launching Master, Slave1, Slave2 MySQL containers..."
docker compose up -d

echo ">> Waiting for MySQL instances to become healthy..."
for i in {1..30}; do
    MASTER_HEALTH=$(docker inspect --format='{{json .State.Health.Status}}' mysql-master 2>/dev/null || echo "\"starting\"")
    SLAVE1_HEALTH=$(docker inspect --format='{{json .State.Health.Status}}' mysql-slave1 2>/dev/null || echo "\"starting\"")
    SLAVE2_HEALTH=$(docker inspect --format='{{json .State.Health.Status}}' mysql-slave2 2>/dev/null || echo "\"starting\"")

    if [ "$MASTER_HEALTH" == "\"healthy\"" ] && [ "$SLAVE1_HEALTH" == "\"healthy\"" ] && [ "$SLAVE2_HEALTH" == "\"healthy\"" ]; then
        echo ">> All MySQL instances are ready and healthy!"
        break
    fi
    sleep 2
done

echo "================================================================"
echo " MySQL Master & Slave instances are RUNNING."
echo "   - Master: localhost:3306"
echo "   - Slave1: localhost:3307"
echo "   - Slave2: localhost:3308"
echo " Streaming MySQL General Logs below..."
echo " Press [Ctrl+C] to stop and remove all MySQL containers."
echo "================================================================"

docker compose logs -f
