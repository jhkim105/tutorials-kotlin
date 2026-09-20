#!/usr/bin/env bash

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$DIR"

echo "================================================================"
echo " Starting Spring Boot Application (spring-routing-datasource)"
echo "================================================================"

cleanup() {
    echo ""
    echo ">> [Cleanup] Terminating Spring Boot server and gradle process..."
    if [ -n "$BOOT_PID" ]; then
        kill -TERM "$BOOT_PID" 2>/dev/null || true
        wait "$BOOT_PID" 2>/dev/null || true
    fi
    ./gradlew --stop > /dev/null 2>&1 || true
    echo ">> Spring Boot server stopped."
    exit 0
}

# Ctrl+C, 종료 시그널 수신 시 프로세스 정리
trap cleanup SIGINT SIGTERM EXIT

echo "================================================================"
echo " Available Endpoints:"
echo "   - POST http://localhost:8080/api/members        (Write -> Master:3306)"
echo "   - GET  http://localhost:8080/api/members/{id}   (Read  -> Slave:3307/3308)"
echo "   - GET  http://localhost:8080/api/members        (Read  -> Slave:3307/3308)"
echo "   - GET  http://localhost:8080/api/routing/write-check"
echo "   - GET  http://localhost:8080/api/routing/read-check"
echo "   - GET  http://localhost:8080/api/routing/pool-status"
echo "================================================================"

./gradlew bootRun &
BOOT_PID=$!
wait "$BOOT_PID"
