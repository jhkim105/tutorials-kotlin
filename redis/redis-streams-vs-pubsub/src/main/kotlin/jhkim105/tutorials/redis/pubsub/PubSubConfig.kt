package jhkim105.tutorials.redis.pubsub

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter

@Configuration
class PubSubConfig {

    @Bean fun pubSubTopic(): ChannelTopic = ChannelTopic("vs:pubsub:channel")

    @Bean fun pubSubSubscriber(): PubSubSubscriber = PubSubSubscriber()

    @Bean
    fun pubSubMessageListener(subscriber: PubSubSubscriber): MessageListenerAdapter =
            MessageListenerAdapter(subscriber)

    @Bean
    fun pubSubListenerContainer(
            connectionFactory: RedisConnectionFactory,
            pubSubMessageListener: MessageListenerAdapter,
            pubSubTopic: ChannelTopic
    ): RedisMessageListenerContainer =
            RedisMessageListenerContainer().apply {
                setConnectionFactory(connectionFactory)
                addMessageListener(pubSubMessageListener, pubSubTopic)
            }
}
