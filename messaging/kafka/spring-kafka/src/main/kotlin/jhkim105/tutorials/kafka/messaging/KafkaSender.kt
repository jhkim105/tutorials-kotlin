package jhkim105.tutorials.kafka.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class KafkaSender(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper
) {

    fun send(topic: String, message: Any) {
        kafkaTemplate.send(topic, toJson(message))
        log.debug {"message sent. topic: $topic, message: $message"}
    }

    fun send(topic: String, key:String, message: Any) {
        kafkaTemplate.send(topic, key, toJson(message))
        log.debug { "message sent. topic: $topic, key: $key, message: $message" }
    }

    private fun toJson(obj: Any): String {
        return objectMapper.writeValueAsString(obj)
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}