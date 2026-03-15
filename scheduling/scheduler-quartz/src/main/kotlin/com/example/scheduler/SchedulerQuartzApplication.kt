package com.example.scheduler

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SchedulerQuartzApplication

fun main(args: Array<String>) {
    runApplication<SchedulerQuartzApplication>(*args)
}
