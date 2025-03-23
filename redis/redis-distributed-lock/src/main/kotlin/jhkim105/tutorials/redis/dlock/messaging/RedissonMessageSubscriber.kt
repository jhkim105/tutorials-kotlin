package jhkim105.tutorials.redis.dlock.messaging

import jhkim105.tutorials.redis.dlock.MessageService
import jhkim105.tutorials.redis.dlock.persistence.IdGenerator
import org.redisson.api.listener.MessageListener
import org.springframework.stereotype.Component
import java.time.Instant


@Component
class RedissonMessageSubscriber(
    private val messageService: MessageService
) : MessageListener<String> {
    override fun onMessage(channel: CharSequence?, message: String?) {
        if (message == null) return

        val publishedAt = Instant.ofEpochMilli(message.toLong())
        messageService.saveMessage(IdGenerator.tsid(), publishedAt)
    }


}