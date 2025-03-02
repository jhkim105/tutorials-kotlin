#!/bin/bash
PORTS=("7000" "7001" "7002" "7003" "7004" "7005")

echo "Stopping all Redis nodes..."
for PORT in "${PORTS[@]}"; do
    echo "Stopping Redis server on port $PORT..."
    redis-cli -p "$PORT" shutdown
done

echo "All Redis nodes stopped!"