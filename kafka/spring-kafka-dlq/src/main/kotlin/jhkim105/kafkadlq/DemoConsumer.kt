package jhkim105.kafkadlq

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.RetryableTopic
import org.springframework.kafka.retrytopic.DltStrategy
import org.springframework.kafka.support.Acknowledgment
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Headers
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.retry.annotation.Backoff
import org.springframework.stereotype.Component

@Component
class DemoConsumer(
    private val om: ObjectMapper,
    @Value("\${app.topic.main}") private val topic: String
) {

    @RetryableTopic(
        attempts = "3", // 최초 처리 + 2번 재시도 = 총 3회 시도
        backoff = Backoff(delay = 1_000, maxDelay = 10_000, multiplier = 3.0),
        dltStrategy = DltStrategy.FAIL_ON_ERROR // DLT 전송 실패 시 예외
    )
    @KafkaListener(topics = ["\${app.topic.main}"], groupId = "\${spring.kafka.consumer.group-id}")
    fun onMessage(
        @Payload raw: String,
        @Headers headers: MessageHeaders,
        ack: Acknowledgment,
        record: ConsumerRecord<String, String>
    ) {
        println("raw: $raw")
        val msg = om.readValue(raw, DemoMessage::class.java)

        // 데모용 예외 트리거
        if (msg.payload.equals("boom", ignoreCase = true)) {
            // 처리 실패 → 재시도 후 실패 시 demo.topic-dlt 로 전송됨
            throw IllegalStateException("Demo failure for id=${msg.id}")
        }

        println("[OK] Consumed: key=${record.key()} value=$msg partition=${record.partition()} offset=${record.offset()}")
        ack.acknowledge()
    }
}