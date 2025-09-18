package jhkim105.kafkadlq

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class DltConsumer {

    @KafkaListener(topics = ["demo.topic-dlt"], groupId = "demo-dlt-group")
    fun onDlt(record: ConsumerRecord<String, String>) {
        println("[DLT] Received: key=${record.key()} value=${record.value()} offset=${record.offset()}")
        // TODO: 여기서 알림/저장/대시보드 적재 등 보정 로직 수행
    }
}
