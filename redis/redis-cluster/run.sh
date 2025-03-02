#!/bin/bash
WORK_DIR=$PWD
PORTS=("7000" "7001" "7002" "7003" "7004" "7005")

for PORT in "${PORTS[@]}"; do
    echo "Starting Redis server on port $PORT..."
    osascript -e "tell application \"Terminal\" to do script \"cd $WORK_DIR/$PORT && redis-server redis.conf\""
#    osascript -e "tell application \"Terminal\" to do script \"cd $WORK_DIR/$PORT && redis-server redis.conf\" in (do script \"\")"
done

echo "Waiting for Redis nodes to start..."
for PORT in "${PORTS[@]}"; do
    while true; do
        sleep 1
        if redis-cli -p "$PORT" ping | grep -q "PONG"; then
            echo "Redis on port $PORT is up!"
            break
        fi
    done
done
echo "All Redis cluster nodes started!"