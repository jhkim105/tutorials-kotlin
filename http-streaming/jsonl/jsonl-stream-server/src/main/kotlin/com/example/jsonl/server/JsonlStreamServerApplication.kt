package com.example.jsonl.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class JsonlStreamServerApplication

fun main(args: Array<String>) {
    runApplication<JsonlStreamServerApplication>(*args)
}
