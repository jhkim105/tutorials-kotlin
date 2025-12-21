package com.example.jsonl.server.service

import com.example.jsonl.server.service.TradeTick
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks

@Component
class TradeEventBus {
    private val sink = Sinks.many()
        .multicast()
        .onBackpressureBuffer<TradeTick>(1000, false)

    val events: Flux<TradeTick> = sink.asFlux()

    fun emit(tick: TradeTick): Sinks.EmitResult {
        return sink.tryEmitNext(tick)
    }
}
