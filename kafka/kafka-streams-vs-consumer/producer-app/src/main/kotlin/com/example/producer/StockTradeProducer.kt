package com.example.producer

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class StockTradeProducer(
    private val kafkaTemplate: KafkaTemplate<String, String>
) {
    fun send(topic: String, message: String) {
        kafkaTemplate.send(topic, message)
        println("📤 Sent to $topic: $message")
    }
}