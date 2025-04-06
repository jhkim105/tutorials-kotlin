package jhkim105.tutorials.redis.dlock.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.PatternTopic

@Configuration
class MessageTopicConfig {

    @Bean
    fun channelTopic(): ChannelTopic {
        return ChannelTopic(TOPIC)
    }

    @Bean
    fun patternTopic(): PatternTopic {
        return PatternTopic("$TOPIC:*")
    }

    companion object {
        const val TOPIC = "pubsub:queue"
    }
}
