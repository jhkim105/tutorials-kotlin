package com.example.trade.server.service

import com.example.trade.common.TradeTick
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.springframework.stereotype.Component

@Component
class TradeEventBus {
    private val _events = MutableSharedFlow<TradeTick>(
        replay = 0,
        extraBufferCapacity = 1000,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val events: SharedFlow<TradeTick> = _events.asSharedFlow()

    suspend fun publish(tick: TradeTick) {
        _events.emit(tick)
    }
}
