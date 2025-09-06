package com.example.consumer

import com.example.common.TradeEvent
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class ConsumerService(
    private val objectMapper: ObjectMapper = ObjectMapper()
) {
    private val previousPrices = ConcurrentHashMap<String, Double>()
    private val log = KotlinLogging.logger {}

    @KafkaListener(topics = ["stock-trades"], groupId = "consumer-app")
    fun listen(record: ConsumerRecord<String, String>) {
        log.info {
            "Consumed: topic=${record.topic()}, " +
                    "partition=${record.partition()}, " +
                    "offset=${record.offset()}, " +
                    "key=${record.key()}," +
                    "payload=${record.value()}"
        }

        val tradeEvent = objectMapper.readValue(record.value(), TradeEvent::class.java)
        log.info { "tradeEvent: $tradeEvent" }

        val previous = previousPrices.put(tradeEvent.symbol, tradeEvent.price)
        if (previous != null && tradeEvent.price >= previous * 1.05) {
            log.info { "상승 감지: ${tradeEvent.symbol} ${previous} -> ${tradeEvent.price}" }
        }
    }
}