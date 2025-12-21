package com.example.trade.client

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.client.WebClient

@SpringBootApplication
@ConfigurationPropertiesScan
class TradeSseClientApplication {
    @Bean
    fun webClientBuilder(): WebClient.Builder = WebClient.builder()
}

fun main(args: Array<String>) {
    runApplication<TradeSseClientApplication>(*args)
}
