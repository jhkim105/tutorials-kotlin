package jhkim105.tutorials.redis.dlock.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import jhkim105.tutorials.redis.dlock.service.MessageService
import jhkim105.tutorials.redis.dlock.controller.SampleMessage
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.stereotype.Component

@Component
class RedisMessageSubscriber(
    private val messageService: MessageService,
    private val objectMapper: ObjectMapper
) : MessageListener {
    override fun onMessage(message: Message, pattern: ByteArray?) {
        val messageString = String(message.body)
        log.info {"Message received: ${String(message.body)}, channel: ${pattern?.let{String(it)}}"}
        val sampleMessage = objectMapper.readValue(messageString, SampleMessage::class.java)
        messageService.saveMessage(sampleMessage.id, sampleMessage.createdAt)
    }

    companion object{
        private val log = KotlinLogging.logger {}
    }
}