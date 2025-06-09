package jhkim105.tutorials.redis

import org.redisson.api.RedissonClient
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener


@Configuration
class EventListenerConfig(
    private val redissonClient: RedissonClient,
    private val subscriber: Subscriber
) {

    @EventListener
    fun onApplicationReadyEvent(applicationReadyEvent: ApplicationReadyEvent) {
        subscribe()
    }

    private fun subscribe() {
        redissonClient.getTopic("LOCAL:TOPIC").run {
            addListener(String::class.java, subscriber)
        }

        redissonClient.getShardedTopic("LOCAL:STOPIC").run {
            addListener(String::class.java, subscriber)
        }

        redissonClient.getShardedTopic("LOCAL:STOPIC").run {
            addListener(String::class.java, subscriber)
        }

        redissonClient.getShardedTopic("LOCAL:STOPIC").run {
            addListener(String::class.java, subscriber)
        }
    }

}