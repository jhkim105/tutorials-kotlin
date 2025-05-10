package jhkim105.tutorials.webclient

import kotlinx.coroutines.reactor.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.util.retry.Retry
import java.time.Duration
import java.time.Instant

@Service
class ClientService(
    private val webClient: WebClient
) {
    private val log = LoggerFactory.getLogger(javaClass)

    suspend fun call(delayMillis: Long): String {
        return webClient.get()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/api/test")
                    .queryParam("delay", delayMillis)
                    .build()
            }
            .retrieve()
            .bodyToMono(String::class.java)
            .awaitSingle()
    }

    suspend fun callAndRetry(delayMillis: Long): String {
        return webClient.get()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/api/test")
                    .queryParam("delay", delayMillis)
                    .build()
            }
            .retrieve()
            .bodyToMono(String::class.java)
            .doOnSubscribe { println("요청 시작") }
            .doOnNext { println("응답 성공") }
            .doOnError { ex -> println("요청 실패: $ex") }
            .retryWhen(
                Retry.backoff(3, Duration.ofSeconds(1))
                    .doBeforeRetry { signal ->
                        println("재시도: attempt=${signal.totalRetries() + 1}, reason=${signal.failure().message}")
                    }
            )
            .onErrorResume { ex ->
                println("모든 재시도 실패, fallback 응답 반환: ${ex.message}")
                Mono.just("fallback-response")
            }
            .awaitSingle()
    }
}