package com.example.producer

data class TradeRequest(
    val symbol: String,
    val price: Double
)