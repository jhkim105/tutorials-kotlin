package com.example.jsonl.client.model

import java.math.BigDecimal


data class TradeTick(
    val symbol: String,
    val price: BigDecimal,
    val qty: Long,
    val tradeId: String,
    val ts: Long
)
