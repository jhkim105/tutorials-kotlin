package jhkim105.tutorials.redis.dlock

import com.fasterxml.jackson.databind.ObjectMapper
import io.hypersistence.utils.hibernate.id.TsidGenerator
import jhkim105.tutorials.redis.dlock.config.MessageTopicConfig.Companion.TOPIC
import jhkim105.tutorials.redis.dlock.messaging.RedisMessagePublisher
import jhkim105.tutorials.redis.dlock.persistence.IdGenerator
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping(path = ["/messages"])
class MessageController(
    private val messagePublisher: RedisMessagePublisher,
    private val objectMapper: ObjectMapper
) {

    @GetMapping("/publish")
    fun publish() {
        val message = SampleMessage(IdGenerator.tsid(), Instant.now())
        messagePublisher.publish(TOPIC, objectMapper.writeValueAsString(message))
    }
}

data class SampleMessage(
    val id: String,
    val createdAt: Instant
)
