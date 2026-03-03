package jhkim105.tutorials.redis.pubsub

import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Instant
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

/**
 * Redis Pub/Sub 데모 컨트롤러
 *
 * - POST /pubsub/publish : 채널에 메시지 발행
 * - GET /pubsub/subscribe: SSE로 실시간 메시지 수신 대기
 *
 * 특성: 구독자가 없을 때 발행된 메시지는 유실됨 (Fire & Forget)
 */
@RestController
@RequestMapping("/pubsub")
class PubSubController(
        private val pubSubPublisher: PubSubPublisher,
        private val pubSubSubscriber: PubSubSubscriber,
        private val pubSubTopic: ChannelTopic
) {

    /**
     * 채널에 메시지 발행
     * - 현재 연결된 모든 구독자에게 즉시 전달
     * - 구독자가 없으면 메시지 유실
     */
    @PostMapping("/publish")
    fun publish(@RequestBody req: PublishRequest): PublishResponse {
        pubSubPublisher.publish(req.message)
        log.info { "POST /pubsub/publish message=${req.message}" }
        return PublishResponse(
                channel = pubSubTopic.topic,
                message = req.message,
                publishedAt = Instant.now().toString()
        )
    }

    /**
     * SSE로 실시간 메시지 수신 대기
     * - 연결 즉시 "connected" 이벤트 전송
     * - 이후 해당 채널에 발행된 메시지를 "message" 이벤트로 실시간 수신
     * - 연결 해제 시 자동 정리
     */
    @GetMapping("/subscribe", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun subscribe(): SseEmitter {
        val emitter = SseEmitter(0L) // 타임아웃 없음
        pubSubSubscriber.registerEmitter(emitter)

        // 연결 확인 이벤트 전송
        emitter.send(
                SseEmitter.event()
                        .name("connected")
                        .data(
                                "Connected to Redis Pub/Sub channel [${pubSubTopic.topic}]. Waiting for messages..."
                        )
        )
        log.info { "GET /pubsub/subscribe — SSE client connected" }
        return emitter
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}

data class PublishRequest(val message: String)

data class PublishResponse(val channel: String, val message: String, val publishedAt: String)
