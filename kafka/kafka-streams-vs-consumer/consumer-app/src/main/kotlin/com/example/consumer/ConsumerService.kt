package com.example.consumer

import com.example.common.PriceEvent
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class ConsumerService(
    private val objectMapper: ObjectMapper = ObjectMapper()
) {
    private val previousPrices = ConcurrentHashMap<String, Double>()

    @KafkaListener(topics = ["stock-prices"], groupId = "consumer-app")
    fun listen(record: ConsumerRecord<String, String>) {
        val event = objectMapper.readValue(record.value(), PriceEvent::class.java)
        val previous = previousPrices.put(event.symbol, event.price)
        if (previous != null && event.price >= previous * 1.05) {
            println("📈 상승 감지: ${event.symbol} ${previous} -> ${event.price}")
        }
    }
}