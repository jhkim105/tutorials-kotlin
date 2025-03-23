package jhkim105.tutorials.redis.pubsub

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter


@Configuration
class RedisConfig {
    @Bean
    fun topic(): ChannelTopic {
        return ChannelTopic("pubsub:queue")
    }

    @Bean
    fun messageListener(): MessageListenerAdapter {
        return MessageListenerAdapter(RedisMessageSubscriber())
    }

    @Bean
    fun messageListenerContainer(connectionFactory: RedisConnectionFactory): RedisMessageListenerContainer {
        return RedisMessageListenerContainer().apply {
            setConnectionFactory(connectionFactory)
            addMessageListener(messageListener(), topic())
        }

    }
}
