package jhkim105.tutorials.springresilience4j.retry

import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import jhkim105.tutorials.springresilience4j.retry.PaymentService
import jhkim105.tutorials.springresilience4j.retry.PaymentServiceImpl
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class PaymentServiceTest @Autowired constructor(
    private val paymentService: PaymentService,       // AOP 적용된 프록시
    private val paymentServiceImpl: PaymentServiceImpl, // 구현체로 내부 상태 확인용
    private val circuitBreakerRegistry: CircuitBreakerRegistry,
) {

    @BeforeEach
    fun setup() {
        paymentServiceImpl.reset()
        circuitBreakerRegistry.circuitBreaker("paymentCircuitBreaker").reset()
    }

    @Test
    fun `should retry and succeed on third attempt`() {
        println("paymentService class = ${paymentService::class.java.name}")
        val result = paymentService.processPayment(1)

        assertEquals("success", result)
        assertEquals(3, paymentServiceImpl.getAttemptCount(1))
    }

    @Test
    fun `should fallback after exceeding max retries`() {
        val result = paymentService.processPayment(999)

        assert(result.startsWith("Fallback for 999"))
        assertEquals(5, paymentServiceImpl.getAttemptCount(999)) // maxAttempts=5
    }

    @Test
    fun `should open circuit breaker after consecutive failures`() {
        repeat(10) {
            paymentService.processPayment(999) // 계속 실패 유도
        }

        val cb = circuitBreakerRegistry.circuitBreaker("paymentCircuitBreaker")
        assertEquals(CircuitBreaker.State.OPEN, cb.state)
    }
}