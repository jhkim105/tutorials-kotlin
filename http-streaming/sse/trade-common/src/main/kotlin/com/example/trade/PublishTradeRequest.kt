package com.example.trade.common

import java.math.BigDecimal

data class PublishTradeRequest(
    val symbol: String,
    val price: BigDecimal,
    val qty: BigDecimal
)
