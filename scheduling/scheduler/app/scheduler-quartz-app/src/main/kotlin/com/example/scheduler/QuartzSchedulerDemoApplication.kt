package com.example.scheduler

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class QuartzSchedulerDemoApplication

fun main(args: Array<String>) {
    runApplication<QuartzSchedulerDemoApplication>(*args)
}
