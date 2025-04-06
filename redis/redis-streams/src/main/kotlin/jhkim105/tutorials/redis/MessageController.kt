package jhkim105.tutorials.redis

import com.fasterxml.jackson.databind.ObjectMapper
import jhkim105.tutorials.redis.config.DEMO_STREAM_KEY
import jhkim105.tutorials.redis.dlock.persistence.IdGenerator
import jhkim105.tutorials.redis.streams.RedisMessageProducer
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping(path = ["/messages"])
class MessageController(
    private val messageProducer: RedisMessageProducer,
    private val objectMapper: ObjectMapper
) {

    @GetMapping("/publish")
    fun publish() {
        val message = SampleMessage(IdGenerator.tsid(), Instant.now())
        messageProducer.sendToStream(DEMO_STREAM_KEY, objectMapper.writeValueAsString(message))
    }
}

data class SampleMessage(
    val id: String,
    val createdAt: Instant
)
