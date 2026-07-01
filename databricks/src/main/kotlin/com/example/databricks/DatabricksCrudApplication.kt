package com.example.databricks

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DatabricksCrudApplication

fun main(args: Array<String>) {
    runApplication<DatabricksCrudApplication>(*args)
}
