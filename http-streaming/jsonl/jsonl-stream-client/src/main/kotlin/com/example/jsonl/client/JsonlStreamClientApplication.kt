package com.example.jsonl.client

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class JsonlStreamClientApplication

fun main(args: Array<String>) {
    runApplication<JsonlStreamClientApplication>(*args)
}
