package jhkim105.tutorials.redis.dlock.messaging

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.stereotype.Service


@Service
class RedisMessagePublisher(
    private val redisTemplate: RedisTemplate<String, String>,
    private val topic: ChannelTopic
) {

    fun publish(message: String) {
        redisTemplate.convertAndSend(topic.topic, message)
    }
}