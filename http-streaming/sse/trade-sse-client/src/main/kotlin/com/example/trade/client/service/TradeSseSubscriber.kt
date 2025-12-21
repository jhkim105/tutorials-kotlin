package com.example.trade.client.service

import com.example.trade.common.TradeTick
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class TradeSseSubscriber(
        private val webClientBuilder: WebClient.Builder,
        private val properties: TradeSseProperties
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var subscriptionJob: Job? = null

    @EventListener(ApplicationReadyEvent::class)
    fun startOnReady() {
        startSubscription()
    }

    private fun startSubscription() {
        if (subscriptionJob?.isActive == true) return
        val webClient = webClientBuilder.baseUrl(properties.serverBaseUrl).build()
        subscriptionJob =
                scope.launch {
                    while (isActive) {
                        try {
                            subscribeOnce(webClient)
                        } catch (ex: Exception) {
                            logger.warn("SSE subscription error: {}", ex.message, ex)
                            delay(2000)
                        }
                    }
                }
    }

    private suspend fun subscribeOnce(webClient: WebClient) {
        val typeRef = object : ParameterizedTypeReference<ServerSentEvent<TradeTick>>() {}
        logger.info(
                "Starting SSE subscription to {}/stream/trades?symbol={}",
                properties.serverBaseUrl,
                properties.symbol
        )
        webClient
                .get()
                .uri { uriBuilder ->
                    uriBuilder
                            .path("/stream/trades")
                            .queryParam("symbol", properties.symbol)
                            .build()
                }
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(typeRef)
                .asFlow()
                .onStart { logger.info("SSE connected symbol={}", properties.symbol) }
                .onCompletion { cause ->
                    val reason = cause?.message ?: "stream completed"
                    logger.info("SSE disconnected symbol={} reason={}", properties.symbol, reason)
                }
                .filterNotNull()
                .collect { event ->
                    val tick = event.data()
                    if (tick != null) {
                        logger.info(
                                "[trade] symbol={} price={} qty={} id={} ts={}",
                                tick.symbol,
                                tick.price,
                                tick.qty,
                                tick.tradeId,
                                tick.ts
                        )
                    }
                }
    }

    fun shutdown() {
        subscriptionJob?.cancel()
        scope.cancel()
        logger.info("SSE subscriber stopped")
    }

    @PreDestroy
    fun onShutdown() {
        shutdown()
    }
}
