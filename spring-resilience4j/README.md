

## Dependencies

```
	implementation("io.github.resilience4j:resilience4j-spring-boot3:2.3.0"))
	implementation("org.springframework.boot:spring-boot-starter-aop")
	implementation("org.springframework.boot:spring-boot-starter-web")
```

## RestController and External API Caller


## Retry
@CircuitBreaker
```kotlin
    @Retry(name = "paymentRetry")
    @CircuitBreaker(name = "paymentCircuitBreaker") // fallback 을 지정하면 retry 가 안된다.
    override fun processPayment(i: Int): String {
        val attempt = attempts.getOrDefault(i, 0) + 1
        attempts[i] = attempt
        log.info("Processing payment for $i, attempt $attempt")

        if (i == 1 && attempt >= 3) {
            return "success"
        }

        throw RuntimeException("Simulated failure at attempt $attempt")
    }
```
@CircuitBreaker(name = "paymentCircuitBreaker", fallbackMethod = "fallback") 처럼 fallbackMethod 를 지정하면 바로 retry 안되고 fallbackMethod 를 실행한다.

### Logging

```kotlin
@Component
class RetryLogger(
    retryRegistry: RetryRegistry
) {
    private val log = KotlinLogging.logger {  }
    init {
        retryRegistry.retry("paymentRetry").eventPublisher
            .onRetry { log.info {"Retrying: ${it.numberOfRetryAttempts}"} }
            .onSuccess { log.info {"Success after retries"} }
            .onError { log.error {"Error after retries"} }
    }
}
```

다른 방법 - RegistryEventConsumer 등록하기
```kotlin
@Configuration
class RetryEventLoggingConfiguration {
    private val log = KotlinLogging.logger {}

    @Bean
    fun registryEventConsumer(): RegistryEventConsumer<Retry> =
        object : RegistryEventConsumer<Retry> {

            override fun onEntryAddedEvent(entryAddedEvent: EntryAddedEvent<Retry>) {
                val retry = entryAddedEvent.addedEntry

                retry.eventPublisher
                    .onRetry { event ->
                        log.info {
                            """
                            | [RETRY] ${event.name}: Attempt ${event.numberOfRetryAttempts}
                            | Reason: ${event.lastThrowable?.message ?: "Unknown"}
                            """.trimMargin()
                        }
                    }
                    .onError { event ->
                        log.error {
                            """
                            | [ERROR] ${event.name}: Retry exhausted
                            | Reason: ${event.lastThrowable?.message ?: "Unknown"}
                            """.trimMargin()
                        }
                    }
                    .onEvent { event ->
                        log.debug { "[EVENT] ${event.name}: $event" }
                    }
            }

            ....
        }
}
```

