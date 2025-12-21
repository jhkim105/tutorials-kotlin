package com.example.jsonl.server.controller

import java.math.BigDecimal

data class PublishTradeRequest(
    val symbol: String,
    val price: BigDecimal,
    val qty: Long
)