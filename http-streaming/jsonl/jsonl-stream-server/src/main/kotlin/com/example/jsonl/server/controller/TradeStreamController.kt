package com.example.jsonl.server.controller

import com.example.jsonl.server.controller.PublishTradeRequest
import com.example.jsonl.server.service.TradeTick
import com.example.jsonl.server.service.TradeEventBus
import com.example.jsonl.server.service.TradeService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import java.time.Instant
import java.util.UUID

@RestController
class TradeStreamController(
    private val tradeService: TradeService,
    private val eventBus: TradeEventBus
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @PostMapping("/api/trades")
    fun publish(@RequestBody request: PublishTradeRequest): ResponseEntity<Void> {
        val tick = TradeTick(
            symbol = request.symbol,
            price = request.price,
            qty = request.qty,
            tradeId = UUID.randomUUID().toString(),
            ts = Instant.now().toEpochMilli()
        )
        tradeService.publish(tick)
        return ResponseEntity.accepted().build()
    }

    @GetMapping("/stream/trades", produces = ["application/x-ndjson"])
    fun streamTrades(@RequestParam symbol: String): Flux<TradeTick> {
        logger.info("NDJSON stream requested symbol={}", symbol)
        return eventBus.events
            .filter { it.symbol == symbol }
            .doOnSubscribe { logger.info("NDJSON stream started symbol={}", symbol) }
            .doFinally { signal -> logger.info("NDJSON stream completed symbol={} signal={}", symbol, signal) }
    }
}
