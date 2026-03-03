package jhkim105.tutorials.redis.pubsub

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.stereotype.Service

@Service
class PubSubPublisher(
        private val redisTemplate: RedisTemplate<String, String>,
        private val pubSubTopic: ChannelTopic
) {
    fun publish(message: String) {
        log.info { "📤 [Pub/Sub] Publishing: $message" }
        redisTemplate.convertAndSend(pubSubTopic.topic, message)
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}
