package com.example.scheduler

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class SchedulerTaskDemoApplication

fun main(args: Array<String>) {
    runApplication<SchedulerTaskDemoApplication>(*args)
}
