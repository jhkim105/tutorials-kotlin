package jhkim105.tutorials.redis.streams

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import jhkim105.tutorials.redis.MessageService
import jhkim105.tutorials.redis.SampleMessage
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.stream.StreamListener
import org.springframework.stereotype.Component

@Component
class RedisMessageConsumer(
    private val messageService: MessageService,
    private val objectMapper: ObjectMapper
) : StreamListener<String, MapRecord<String, String, String>> {
    override fun onMessage(message: MapRecord<String, String, String>) {
        val data = message.value
        log.info {"Message received: $data"}
        val jsonString = objectMapper.writeValueAsString(data)
        val sampleMessage = objectMapper.readValue(jsonString, SampleMessage::class.java)
        messageService.saveMessage(sampleMessage.id, sampleMessage.createdAt)
    }

    companion object{
        private val log = KotlinLogging.logger {}
    }
}