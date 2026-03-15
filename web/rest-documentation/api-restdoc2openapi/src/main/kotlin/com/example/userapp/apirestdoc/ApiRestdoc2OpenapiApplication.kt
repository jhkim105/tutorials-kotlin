package com.example.userapp.apirestdoc

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.example.userapp"])
class ApiRestdoc2OpenapiApplication

fun main(args: Array<String>) {
    runApplication<ApiRestdoc2OpenapiApplication>(*args)
}
