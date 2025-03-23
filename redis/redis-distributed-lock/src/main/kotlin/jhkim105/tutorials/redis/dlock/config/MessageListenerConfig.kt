package jhkim105.tutorials.redis.dlock.config

import jhkim105.tutorials.redis.dlock.messaging.RedisMessageSubscriber
import jhkim105.tutorials.redis.dlock.messaging.RedissonMessageSubscriber
import org.redisson.api.RedissonClient
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter

@Configuration
class MessageListenerConfig(
    private val redissonClient: RedissonClient,
    private val redissonMessageSubscriber: RedissonMessageSubscriber,
    private val messageSubscriber: RedisMessageSubscriber
) {

    @EventListener
    fun applicationReadyEvent(applicationReadyEvent: ApplicationReadyEvent) {
        redissonClient.getShardedTopic(TOPIC).run {
            addListener(String::class.java, redissonMessageSubscriber)
        }
    }


    @Bean
    fun topic(): ChannelTopic {
        return ChannelTopic(TOPIC)
    }

    @Bean
    fun messageListener(): MessageListenerAdapter {
        return MessageListenerAdapter(messageSubscriber)
    }

    @Bean
    fun messageListenerContainer(redisConnectionFactory: RedisConnectionFactory): RedisMessageListenerContainer {
        return RedisMessageListenerContainer().apply {
            setConnectionFactory(redisConnectionFactory)
            addMessageListener(messageListener(), topic())
        }

    }

    companion object {
        const val TOPIC = "pubsub:queue"
    }
}