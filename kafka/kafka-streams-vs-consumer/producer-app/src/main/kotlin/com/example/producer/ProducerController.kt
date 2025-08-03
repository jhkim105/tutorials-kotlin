package com.example.producer

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/trades")
class ProducerController(
    private val producer: StockTradeProducer,
    private val objectMapper: ObjectMapper
) {
    @PostMapping
    fun sendTrade(@RequestBody request: TradeRequest): String {
        val json = objectMapper.writeValueAsString(request)
        producer.send("stock-trades", json)
        return "OK"
    }
}