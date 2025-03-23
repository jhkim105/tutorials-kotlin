package jhkim105.tutorials.redis.dlock.messaging

import jhkim105.tutorials.redis.dlock.MessageService
import jhkim105.tutorials.redis.dlock.persistence.IdGenerator
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class RedisMessageSubscriber(
    private val messageService: MessageService
) : MessageListener {
    override fun onMessage(message: Message, pattern: ByteArray?) {
        val messageString = String(message.body)
        println("Message received: ${String(message.body)}")
        val publishedAt = Instant.ofEpochMilli(messageString.toLong())
        messageService.saveMessage(IdGenerator.tsid(), publishedAt)
    }


}