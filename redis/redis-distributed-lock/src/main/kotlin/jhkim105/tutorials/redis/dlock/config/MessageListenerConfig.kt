package jhkim105.tutorials.redis.dlock.config

import jhkim105.tutorials.redis.dlock.messaging.RedisMessageSubscriber
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.PatternTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter

@Configuration
class MessageListenerConfig(
    private val messageSubscriber: RedisMessageSubscriber,
    private val topic: ChannelTopic,
) {


    @Bean
    fun messageListener(): MessageListenerAdapter {
        return MessageListenerAdapter(messageSubscriber)
    }

    @Bean
    fun messageListenerContainer(redisConnectionFactory: RedisConnectionFactory): RedisMessageListenerContainer {
        return RedisMessageListenerContainer().apply {
            setConnectionFactory(redisConnectionFactory)
            addMessageListener(messageListener(), topic)
        }

    }

}