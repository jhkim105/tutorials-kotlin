package com.example.demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SpringKotlinMvcApplication

fun main(args: Array<String>) {
    runApplication<SpringKotlinMvcApplication>(*args)
}
