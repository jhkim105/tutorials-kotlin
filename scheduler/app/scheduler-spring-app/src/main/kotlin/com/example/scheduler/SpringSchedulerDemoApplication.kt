package com.example.scheduler

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class SpringSchedulerDemoApplication

fun main(args: Array<String>) {
    runApplication<SpringSchedulerDemoApplication>(*args)
}
