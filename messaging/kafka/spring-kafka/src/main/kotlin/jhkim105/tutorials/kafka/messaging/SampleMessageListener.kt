package jhkim105.tutorials.kafka.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import jhkim105.tutorials.kafka.MessageService
import jhkim105.tutorials.kafka.SampleMessage
import jhkim105.tutorials.kafka.config.KafkaConfig.Topics.SAMPLE_TOPIC
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
class SampleMessageListener(
    private val messageService: MessageService,
    private val objectMapper: ObjectMapper
) {

    @KafkaListener(topics = [SAMPLE_TOPIC])
    fun handle(@Payload message: String, @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int) {
        logger.debug("message received. message: {}, partition: {}", message, partition)
        val sampleMessage = objectMapper.readValue(message, SampleMessage::class.java)
        messageService.saveMessage(sampleMessage.id, sampleMessage.createdAt)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SampleMessageListener::class.java)
    }
}