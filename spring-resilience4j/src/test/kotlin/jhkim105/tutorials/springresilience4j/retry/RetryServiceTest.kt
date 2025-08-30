package jhkim105.tutorials.springresilience4j.retry

import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class RetryServiceTest @Autowired constructor(
    private val retryService: RetryService,       // AOP 적용된 프록시
    private val retryServiceImpl: RetryServiceImpl, // 구현체로 내부 상태 확인용
//    private val circuitBreakerRegistry: CircuitBreakerRegistry,
) {

    @BeforeEach
    fun setup() {
        retryServiceImpl.reset()
//        circuitBreakerRegistry.circuitBreaker("paymentCircuitBreaker").reset()
    }

    @Test
    fun `should retry and succeed on third attempt`() {
        println("paymentService class = ${retryService::class.java.name}")
        val result = retryService.processSomething(1)

        Assertions.assertEquals("success", result)
        Assertions.assertEquals(3, retryServiceImpl.getAttemptCount(1))
    }

    @Test
    fun `should fallback after exceeding max retries`() {
        val result = retryService.processSomething(999)

        assert(result.startsWith("Fallback for 999"))
        Assertions.assertEquals(5, retryServiceImpl.getAttemptCount(999)) // maxAttempts=5
    }

//    @Test
//    fun `should open circuit breaker after consecutive failures`() {
//        repeat(10) {
//            retryService.processSomething(999) // 계속 실패 유도
//        }
//
//        val cb = circuitBreakerRegistry.circuitBreaker("paymentCircuitBreaker")
//        Assertions.assertEquals(CircuitBreaker.State.OPEN, cb.state)
//    }
}