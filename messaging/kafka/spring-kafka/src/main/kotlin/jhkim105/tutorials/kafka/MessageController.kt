package jhkim105.tutorials.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import jhkim105.tutorials.kafka.config.KafkaConfig.Topics.SAMPLE_TOPIC
import jhkim105.tutorials.kafka.messaging.KafkaSender
import jhkim105.tutorials.kafka.persistence.IdGenerator
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping(path = ["/messages"])
class MessageController(
    private val kafkaSender: KafkaSender
) {

    @GetMapping("/publish")
    fun publish() {
        val message = SampleMessage(IdGenerator.tsid(), Instant.now())
        kafkaSender.send(SAMPLE_TOPIC, message)
    }
}

data class SampleMessage(
    val id: String,
    val createdAt: Instant
)
