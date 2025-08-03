package com.example.streams

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.example.common.TradeEvent
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Materialized
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafkaStreams

@Configuration
@EnableKafkaStreams
class StreamConfig {
    
    private val objectMapper = jacksonObjectMapper()

    @Bean
    fun kStream(builder: StreamsBuilder): KStream<String, String> {
        val stream = builder.stream("stock-trades", Consumed.with(Serdes.String(), Serdes.String()))

        stream
            .groupByKey()
            .aggregate(
                { null as String? },
                { _, newEventJson, oldEventJson ->
                    try {
                        val newEvent: TradeEvent = objectMapper.readValue(newEventJson)
                        
                        if (oldEventJson != null) {
                            val oldEvent: TradeEvent = objectMapper.readValue(oldEventJson)
                            val change = (newEvent.price - oldEvent.price) / oldEvent.price * 100
                            if (kotlin.math.abs(change) >= 5.0) {
                                println("🔔 STREAM - ${newEvent.symbol} 가격 변동 감지: ${oldEvent.price} -> ${newEvent.price} (${String.format("%.2f", change)}%)")
                            }
                        }
                        
                        newEventJson
                    } catch (e: Exception) {
                        println("Error processing event: ${e.message}")
                        newEventJson
                    }
                },
                Materialized.with(Serdes.String(), Serdes.String())
            )

        return stream
    }
}