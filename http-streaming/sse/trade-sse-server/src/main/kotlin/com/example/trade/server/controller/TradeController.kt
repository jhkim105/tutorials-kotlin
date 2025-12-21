package com.example.trade.server.controller

import com.example.trade.common.PublishTradeRequest
import com.example.trade.common.TradeTick
import com.example.trade.server.service.TradeEventBus
import com.example.trade.server.service.TradeService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class TradeController(private val tradeService: TradeService, private val eventBus: TradeEventBus) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @PostMapping("/api/trades")
    suspend fun publish(@RequestBody request: PublishTradeRequest): ResponseEntity<Void> {
        tradeService.publish(request)
        return ResponseEntity.accepted().build()
    }

    @GetMapping("/stream/trades", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamTrades(@RequestParam symbol: String): Flow<ServerSentEvent<TradeTick>> {
        logger.info("New SSE subscription for symbol: {}", symbol)

        return eventBus.events
                .filter { it.symbol == symbol }
                .map { tick ->
                    ServerSentEvent.builder(tick).id(tick.tradeId).event("trade").build()
                }
                .onStart { logger.info("SSE stream started for symbol: {}", symbol) }
                .onCompletion { logger.info("SSE stream completed for symbol: {}", symbol) }
    }
}
