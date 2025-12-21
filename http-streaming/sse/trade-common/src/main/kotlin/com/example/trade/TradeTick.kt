package com.example.trade.common

import java.math.BigDecimal
import java.time.Instant

data class TradeTick(
    val tradeId: String,
    val symbol: String,
    val price: BigDecimal,
    val qty: BigDecimal,
    val ts: Instant
)
