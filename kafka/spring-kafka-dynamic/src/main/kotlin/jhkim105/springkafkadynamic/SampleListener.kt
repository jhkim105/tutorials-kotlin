package jhkim105.springkafkadynamic

import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
class SampleListener {

    @KafkaListener(
        id = "orders-listener",
        topics = ["orders"],
        groupId = "order-consumers",
        autoStartup = "false", // 앱 시작 시 자동 기동 안 함
        concurrency = "2"
    )
    fun onMessage(@Payload payload: String) {
        // 메시지 처리
        println("orders-listener received: $payload")
    }
}