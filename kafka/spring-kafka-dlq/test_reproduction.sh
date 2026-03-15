#!/bin/bash

echo "=== Testing Spring Kafka DLQ Application ==="
echo "Current directory: $(pwd)"

# Change to the spring-kafka-dlq directory
cd /Users/jihwankim/dev/my/tutorials-kotlin/kafka/spring-kafka-dlq

echo "=== Building the application ==="
./gradlew build

if [ $? -ne 0 ]; then
    echo "Build failed!"
    exit 1
fi

echo "=== Starting the application in background ==="
./gradlew bootRun &
APP_PID=$!

echo "Application PID: $APP_PID"
echo "Waiting 30 seconds for application to start..."
sleep 30

echo "=== Testing if application is running ==="
curl -f http://localhost:8080/actuator/health 2>/dev/null
if [ $? -eq 0 ]; then
    echo "Application is running!"
else
    echo "Application health check failed, trying to send message anyway..."
fi

echo "=== Sending test message ==="
curl -X POST http://localhost:8080/api/messages \
  -H "Content-Type: application/json" \
  -d '{"id":"1","payload":"hello"}' \
  -v

echo ""
echo "=== Waiting 10 seconds to see consumer logs ==="
sleep 10

echo "=== Stopping application ==="
kill $APP_PID
wait $APP_PID 2>/dev/null

echo "=== Test completed ==="