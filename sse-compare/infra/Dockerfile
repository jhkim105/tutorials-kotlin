# syntax=docker/dockerfile:1
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /workspace

ARG MODULE=app-mvc-sseemitter
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
COPY common ./common
COPY persistence-jpa ./persistence-jpa
COPY persistence-r2dbc ./persistence-r2dbc
COPY app-mvc-sseemitter ./app-mvc-sseemitter
COPY app-mvc-flow ./app-mvc-flow
COPY app-webflux-flux ./app-webflux-flux
COPY app-webflux-flow ./app-webflux-flow
COPY loadgen ./loadgen

RUN chmod +x gradlew && ./gradlew :${MODULE}:bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre
WORKDIR /app
ARG MODULE=app-mvc-sseemitter
COPY --from=builder /workspace/${MODULE}/build/libs/*.jar app.jar

ENV JAVA_OPTS="-Xmx512m -Xms512m -XX:+UseG1GC"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
