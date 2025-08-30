package jhkim105.tutorials.springresilience4j.retry

import io.github.resilience4j.retry.Retry
import io.github.resilience4j.retry.RetryConfig
import org.junit.jupiter.api.assertThrows
import java.time.Duration
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals

class RetryTest {

    @Test
    fun `should retry until success`() {
        // given
        val retryConfig = RetryConfig.custom<Any>()
            .maxAttempts(5)
            .waitDuration(Duration.ofMillis(100))
            .build()

        val retry = Retry.of("sampleRetry", retryConfig)

        val service = SampleService()
        val decorated = Retry.decorateSupplier(retry) { service.unreliableMethod() }

        // when
        val result = decorated.get()

        // then
        assertEquals("Success on attempt 3", result)
        assertEquals(3, retry.metrics.numberOfFailedCallsWithRetryAttempt)
    }

    @Test
    fun `should fail after max retries`() {
        val retryConfig = RetryConfig.custom<Any>()
            .maxAttempts(3)
            .waitDuration(Duration.ofMillis(50))
            .build()

        val retry = Retry.of("failRetry", retryConfig)

        var attempt = 0
        val alwaysFail = Retry.decorateSupplier(retry) {
            attempt++
            throw RuntimeException("Always failing (attempt $attempt)")
        }

        val ex = assertThrows<RuntimeException> {
            alwaysFail.get()
        }

        assertEquals("Always failing (attempt 3)", ex.message)
        assertEquals(3, attempt)
    }

    class SampleService {
        private val counter = AtomicInteger(0)

        fun unreliableMethod(): String {
            val attempt = counter.incrementAndGet()
            if (attempt < 3) {
                throw RuntimeException("Failure on attempt $attempt")
            }
            return "Success on attempt $attempt"
        }
    }
}