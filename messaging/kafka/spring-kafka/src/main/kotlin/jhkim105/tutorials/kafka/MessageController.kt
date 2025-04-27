package jhkim105.tutorials.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import jhkim105.tutorials.kafka.config.KafkaConfig.Topics.SAMPLE_COMPACT_TOPIC
import jhkim105.tutorials.kafka.config.KafkaConfig.Topics.SAMPLE_TOPIC
import jhkim105.tutorials.kafka.messaging.KafkaSender
import jhkim105.tutorials.kafka.persistence.IdGenerator
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalField

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

    @GetMapping("/publish_with_key")
    fun publishWithKey() {
        val now = Instant.now()
        val key = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
        val message = SampleMessage(key, now)
        kafkaSender.send(SAMPLE_COMPACT_TOPIC, key, message)
    }
}

data class SampleMessage(
    val key: String,
    val createdAt: Instant
)
