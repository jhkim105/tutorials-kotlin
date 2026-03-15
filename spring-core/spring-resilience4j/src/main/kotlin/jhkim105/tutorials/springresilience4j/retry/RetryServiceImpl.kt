package jhkim105.tutorials.springresilience4j.retry

import io.github.resilience4j.retry.annotation.Retry
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
@Service
class RetryServiceImpl() : RetryService {

    private val log = LoggerFactory.getLogger(javaClass)
    private val attempts = mutableMapOf<Int, Int>()

    @Retry(name = "paymentRetry", fallbackMethod = "fallback")
     override fun processSomething(id: Int, maxRetry: Int): String {
        val attempt = attempts.getOrDefault(id, 0) + 1
        attempts[id] = attempt
        log.info("Processing for $id, attempt $attempt")

        if (id == 1 && attempt >= maxRetry) {
            return "success"
        }

        throw RuntimeException("Simulated failure at attempt $attempt")
    }

    // fallback method 는 원래 method 와 signature 가 일치 해야 한다.
      fun fallback(id: Int, maxRetry: Int, ex: Throwable): String {
        log.warn("Fallback called for $id due to: ${ex.message}")
        return "Fallback for $id due to: ${ex.message}"
    }

    fun reset() {
        attempts.clear()
    }

    fun getAttemptCount(i: Int): Int {
        return attempts.getOrDefault(i, 0)
    }
}