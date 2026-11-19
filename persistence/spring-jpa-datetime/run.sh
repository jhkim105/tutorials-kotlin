#!/usr/bin/env bash
set -e

echo "==> Building Spring Boot JAR..."
./gradlew bootJar -x test

echo "==> Starting Docker Compose environment..."
docker compose up --build
