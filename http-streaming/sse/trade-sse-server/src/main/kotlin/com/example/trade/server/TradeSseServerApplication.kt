package com.example.trade.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TradeSseServerApplication

fun main(args: Array<String>) {
    runApplication<TradeSseServerApplication>(*args)
}
