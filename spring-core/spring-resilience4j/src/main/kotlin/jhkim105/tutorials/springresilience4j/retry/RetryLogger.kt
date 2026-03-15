package jhkim105.tutorials.springresilience4j.retry

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.slf4j.internal.Slf4jLogger
import io.github.resilience4j.retry.RetryRegistry
import org.springframework.stereotype.Component

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