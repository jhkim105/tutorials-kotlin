package jhkim105.kafkadlq

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/messages")
class DemoProducer(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val om: ObjectMapper,
    @Value("\${app.topic.main}") private val topic: String
) {

    @PostMapping
    fun send(@RequestBody msg: DemoMessage): String {
        val value = om.writeValueAsString(msg)
        val record = ProducerRecord(topic, msg.id, value)
        kafkaTemplate.send(record)
        return "sent to $topic: $value"
    }
}
