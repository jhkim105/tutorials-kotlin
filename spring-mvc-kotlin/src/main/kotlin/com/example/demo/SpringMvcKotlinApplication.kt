package com.example.demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SpringMvcKotlinApplication

fun main(args: Array<String>) {
    runApplication<SpringMvcKotlinApplication>(*args)
}
