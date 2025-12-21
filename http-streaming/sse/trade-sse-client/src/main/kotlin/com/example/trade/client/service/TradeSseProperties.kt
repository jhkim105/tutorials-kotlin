package com.example.trade.client.service

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("trade-sse")
data class TradeSseProperties(
    val serverBaseUrl: String,
    val symbol: String
)
