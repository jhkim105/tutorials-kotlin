package jhkim105.springretry

import org.slf4j.LoggerFactory
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service

@Service
class FlakyRemoteClientAnnotation {

    private val log = LoggerFactory.getLogger(javaClass)

    // 데모를 위해: 앞의 실패 횟수를 컨트롤
    @Volatile private var failuresLeft = 2

    fun presetFailures(times: Int) {
        failuresLeft = times
    }

    @Retryable(
        include = [FlakyException::class],
        maxAttempts = 3,                                   // 총 3회 시도 (기본 1 + 재시도 2)
        backoff = Backoff(delay = 200, multiplier = 2.0)   // 200ms, 400ms 지수 백오프
    )
    fun call(request: Request): Response {
        log.info("Annotation call() invoked, failuresLeft=$failuresLeft")
        if (failuresLeft > 0) {
            failuresLeft--
            throw FlakyException("Temporary failure")
        }
        return Response("OK via annotation for ${request.id}")
    }

    // 모든 재시도가 실패했을 때 호출되는 폴백
    @Recover
    fun recover(e: FlakyException, request: Request): Response {
        log.warn("Recover triggered: ${e.message}")
        return Response("FALLBACK via annotation for ${request.id}")
    }
}
