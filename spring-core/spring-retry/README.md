
# Spring Retry

spring retry 
- annotations
- RetryTemplate
- callbacks

## Dependencies

```
    // https://mvnrepository.com/artifact/org.springframework.retry/spring-retry
    implementation("org.springframework.retry:spring-retry:2.0.12")
    implementation("org.springframework.boot:spring-boot-starter-aop:3.5.5")
```

## Configuration

```kotlin
@EnableRetry
class SpringRetryApplication
```