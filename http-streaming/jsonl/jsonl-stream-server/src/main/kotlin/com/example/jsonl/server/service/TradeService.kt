package com.example.jsonl.server.service

import com.example.jsonl.server.controller.PublishTradeRequest
import com.example.jsonl.server.service.TradeTick
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class TradeService(
    private val eventBus: TradeEventBus
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun publish(tick: TradeTick) {
        val result = eventBus.emit(tick)
        if (result.isFailure) {
            logger.warn(
                "Failed to publish trade symbol={} reason={}",
                tick.symbol,
                result
            )
        }
        logger.info(
            "Publishing trade symbol={} price={} qty={} id={}",
            tick.symbol,
            tick.price,
            tick.qty,
            tick.tradeId
        )
    }
}
