package jhkim105.tutorials.redis.pubsub

import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.CopyOnWriteArrayList
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

/**
 * Pub/Sub 구독자.
 *
 * 수신된 메시지를 [receivedMessages]에 저장하고, 등록된 SSE Emitter로 실시간 전송합니다.
 *
 * Pub/Sub 특성:
 * - 구독자가 오프라인 상태였을 때 발행된 메시지는 수신 불가 (메시지 유실)
 * - 메시지는 발행 시점에 연결된 구독자에게만 전달됨
 */
class PubSubSubscriber : MessageListener {

    val receivedMessages: MutableList<String> = CopyOnWriteArrayList()
    private val emitters: MutableList<SseEmitter> = CopyOnWriteArrayList()

    fun registerEmitter(emitter: SseEmitter) {
        emitters.add(emitter)
        emitter.onCompletion { emitters.remove(emitter) }
        emitter.onTimeout { emitters.remove(emitter) }
        emitter.onError { emitters.remove(emitter) }
        log.info { "SSE emitter registered. total=${emitters.size}" }
    }

    override fun onMessage(message: Message, pattern: ByteArray?) {
        val body = String(message.body)
        log.info { "📨 [Pub/Sub] Received: $body" }
        receivedMessages.add(body)

        // 등록된 모든 SSE Emitter로 실시간 전송
        val failedEmitters = mutableListOf<SseEmitter>()
        emitters.forEach { emitter ->
            try {
                emitter.send(SseEmitter.event().name("message").data(body))
            } catch (e: Exception) {
                log.warn { "SSE emitter send failed, removing: ${e.message}" }
                failedEmitters.add(emitter)
            }
        }
        emitters.removeAll(failedEmitters.toSet())
    }

    fun clear() = receivedMessages.clear()

    companion object {
        private val log = KotlinLogging.logger {}
    }
}
