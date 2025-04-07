package jhkim105.tutorials.kafka.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class KafkaSender(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper
) {

    fun send(topic: String, message: Any) {
        kafkaTemplate.send(topic, toJson(message))
        logger.debug("message sent. topic: {}, message: {}", topic, message)
    }

    private fun toJson(obj: Any): String {
        return objectMapper.writeValueAsString(obj)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(KafkaSender::class.java)
    }
}