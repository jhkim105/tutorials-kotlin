package com.example.jsonl.client.service

import com.example.jsonl.client.config.ClientProperties
import com.example.jsonl.client.model.TradeTick
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.Disposable

@Component
class TradeStreamSubscriber(
    private val webClientBuilder: WebClient.Builder,
    private val properties: ClientProperties
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private var subscription: Disposable? = null

    @EventListener(ApplicationReadyEvent::class)
    fun start() {
        if (subscription != null) return
        val client = webClientBuilder.baseUrl(properties.server.baseUrl).build()
        val ndjson = MediaType.parseMediaType("application/x-ndjson")

        subscription = client.get()
            .uri { builder ->
                builder.path("/stream/trades")
                    .queryParam("symbol", properties.symbol)
                    .build()
            }
            .accept(ndjson)
            .retrieve()
            .bodyToFlux(TradeTick::class.java)
            .doOnSubscribe {
                logger.info(
                    "Starting NDJSON subscription url={}/stream/trades?symbol={}",
                    properties.server.baseUrl,
                    properties.symbol
                )
            }
            .doOnNext { tick ->
                logger.info(
                    "[trade] symbol={} price={} qty={} id={} ts={}",
                    tick.symbol,
                    tick.price,
                    tick.qty,
                    tick.tradeId,
                    tick.ts
                )
            }
            .doOnError { ex ->
                logger.warn("NDJSON subscription error: {}", ex.message)
            }
            .doFinally { signal ->
                logger.info("NDJSON subscription finished signal={}", signal)
            }
            .subscribe()
    }

    @PreDestroy
    fun shutdown() {
        subscription?.dispose()
        logger.info("NDJSON subscriber stopped")
    }
}
