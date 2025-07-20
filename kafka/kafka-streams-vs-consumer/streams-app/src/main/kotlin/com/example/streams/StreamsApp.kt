package com.example.streams

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class StreamsApp

fun main(args: Array<String>) {
    runApplication<StreamsApp>(*args)
}