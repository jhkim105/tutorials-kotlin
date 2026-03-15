package com.example.userapp.apirestdoc

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.example.userapp"])
class ApiRestdocApplication

fun main(args: Array<String>) {
    runApplication<ApiRestdocApplication>(*args)
}
