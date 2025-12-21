package com.example.trade.server.service

import com.example.trade.common.PublishTradeRequest
import com.example.trade.common.TradeTick
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class TradeService(
    private val eventBus: TradeEventBus
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    suspend fun publish(request: PublishTradeRequest) {
        val tick = TradeTick(
            tradeId = UUID.randomUUID().toString(),
            symbol = request.symbol,
            price = request.price,
            qty = request.qty,
            ts = Instant.now()
        )
        
        logger.info("Publishing trade: {}", tick)
        eventBus.publish(tick)
    }
}
