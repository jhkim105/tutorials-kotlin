package com.example.streams

import com.example.common.TradeEvent
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Produced
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafkaStreams
import java.util.concurrent.ConcurrentHashMap

//@Configuration
//@EnableKafkaStreams
class StreamsTopology {

    private val objectMapper = jacksonObjectMapper()
    private val previousPrices = ConcurrentHashMap<String, Double>()

    @Bean
    fun kStream(streamsBuilder: StreamsBuilder): KStream<String, String> {
        return streamsBuilder
            .stream("stock-trades", Consumed.with(Serdes.String(), Serdes.String()))
            .peek { key, value -> println("🔄 입력 수신: $key = $value") }
            .filter { _, value -> value != null }
            .mapValues { value ->
                val event: TradeEvent = objectMapper.readValue(value)
                val previous = previousPrices.put(event.symbol, event.price)
                if (previous != null && event.price >= previous * 1.05) {
                    println("📈 스트림 상승 감지: ${event.symbol} ${previous} -> ${event.price}")
                    "ALERT: ${event.symbol} price jumped to ${event.price}"
                } else {
                    "" // Use empty string instead of null
                }
            }
            .filter { _, alert -> alert.isNotEmpty() } // Filter out empty strings
            .also {
                it.to("alerts", Produced.with(Serdes.String(), Serdes.String()))
            }
    }
}