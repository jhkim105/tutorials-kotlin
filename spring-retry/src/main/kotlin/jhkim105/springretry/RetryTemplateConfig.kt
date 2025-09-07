package jhkim105.springretry

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.retry.RetryCallback
import org.springframework.retry.RetryContext
import org.springframework.retry.RecoveryCallback
import org.springframework.retry.RetryListener
import org.springframework.retry.backoff.ExponentialBackOffPolicy
import org.springframework.retry.policy.SimpleRetryPolicy
import org.springframework.retry.support.RetryTemplate
import org.springframework.stereotype.Service

@Configuration
class RetryTemplateConfig {

    @Bean
    fun retryTemplate(): RetryTemplate {
        val template = RetryTemplate()

        val backoff = ExponentialBackOffPolicy().apply {
            initialInterval = 200
            multiplier = 2.0
            maxInterval = 2000
        }
        template.setBackOffPolicy(backoff)

        val retryPolicy = SimpleRetryPolicy(
            3,                                  // 총 3회 시도
            mapOf(FlakyException::class.java to true)
        )
        template.setRetryPolicy(retryPolicy)

        template.setListeners(arrayOf(object : RetryListener {
            override fun <T, E : Throwable?> open(context: RetryContext, callback: RetryCallback<T, E>): Boolean {
                return true
            }
            override fun <T, E : Throwable?> onError(
                context: RetryContext, callback: RetryCallback<T, E>, throwable: Throwable
            ) {
                // 각 시도 실패 시 로깅
            }
            override fun <T, E : Throwable?> close(
                context: RetryContext, callback: RetryCallback<T, E>, throwable: Throwable?
            ) { }
        }))

        return template
    }
}

@Service
class FlakyRemoteClientTemplate(private val retryTemplate: RetryTemplate) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Volatile private var failuresLeft = 2
    fun presetFailures(times: Int) { failuresLeft = times }

    fun call(request: Request): Response {
        return retryTemplate.execute(
            RetryCallback<Response, RuntimeException> {
                log.info("Template call() invoked, failuresLeft=$failuresLeft")
                if (failuresLeft > 0) {
                    failuresLeft--
                    throw FlakyException("Temporary failure")
                }
                Response("OK via template for ${request.id}")
            },
            RecoveryCallback { context ->
                log.warn("RecoveryCallback triggered: ${context.lastThrowable?.message}")
                Response("FALLBACK via template for ${request.id}")
            }
        )
    }
}
