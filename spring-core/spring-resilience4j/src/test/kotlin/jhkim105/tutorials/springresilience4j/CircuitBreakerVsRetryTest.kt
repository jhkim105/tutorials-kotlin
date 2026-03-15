package jhkim105.tutorials.springresilience4j

import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.core.IntervalFunction
import io.github.resilience4j.retry.Retry
import io.github.resilience4j.retry.RetryConfig
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import java.util.concurrent.atomic.AtomicInteger

class CircuitBreakerVsRetryTest {

    interface PaymentService {
        fun processPayment(i: Int): String
    }

    private lateinit var paymentService: PaymentService

    @BeforeEach
    fun setUp() {
        paymentService = mockk()
    }

    @Test
    fun `retry with exponential backoff retries and succeeds`() {
        val intervalFn = IntervalFunction.ofExponentialBackoff(1000L, 2.0)
        val retryConfig = RetryConfig.custom<Any>()
            .maxAttempts(5)
            .intervalFunction(intervalFn)
            .build()

        val retry = Retry.of("paymentRetry", retryConfig)

        every { paymentService.processPayment(1) }
            .throws(RuntimeException("First"))
            .andThenThrows(RuntimeException("Second"))
            .andThen("Success")

        val callable = Retry.decorateCallable(retry) { paymentService.processPayment(1) }

        val result = try {
            callable.call()
        } catch (e: Exception) {
            null
        }

        assertEquals("Success", result)
        verify(exactly = 3) { paymentService.processPayment(1) }
    }

    @Test
    fun `circuit breaker opens after failures and closes after success`() {
        val cbConfig = CircuitBreakerConfig.custom()
            .failureRateThreshold(50f)
            .slidingWindowSize(5)
            .permittedNumberOfCallsInHalfOpenState(3)
            .build()

        val circuitBreaker = CircuitBreaker.of("paymentCB", cbConfig)
        val callCount = AtomicInteger(0)

        every { paymentService.processPayment(any()) } answers {
            callCount.incrementAndGet()
            throw RuntimeException("fail")
        }

        val callable = CircuitBreaker.decorateCallable(circuitBreaker) {
            paymentService.processPayment(1)
        }

        repeat(10) {
            try {
                callable.call()
            } catch (_: Exception) {
            }
        }

        assertEquals(5, callCount.get())
        assertEquals(CircuitBreaker.State.OPEN, circuitBreaker.state)

        callCount.set(0)
        circuitBreaker.transitionToHalfOpenState()
        assertEquals(CircuitBreaker.State.HALF_OPEN, circuitBreaker.state)

        clearMocks(paymentService)
        every { paymentService.processPayment(any()) } answers {
            callCount.incrementAndGet()
            "Success"
        }

        repeat(3) {
            try {
                callable.call()
            } catch (_: Exception) {
            }
        }

        assertEquals(3, callCount.get())
        assertEquals(CircuitBreaker.State.CLOSED, circuitBreaker.state)
    }
}