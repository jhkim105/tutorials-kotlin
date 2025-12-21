package com.example.jsonl.client.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties(prefix = "app")
data class ClientProperties(
    val server: Server = Server(),
    val symbol: String = "AAPL"
) {
    data class Server(
        val baseUrl: String = "http://localhost:8080"
    )
}
