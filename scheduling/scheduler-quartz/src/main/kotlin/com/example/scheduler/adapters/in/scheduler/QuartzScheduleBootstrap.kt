package com.example.scheduler.adapters.`in`.scheduler

import com.example.scheduler.core.application.port.`in`.ScheduleBootstrapUseCase
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

@Component
class QuartzScheduleBootstrap(
    private val scheduleBootstrapUseCase: ScheduleBootstrapUseCase
) : ApplicationRunner {

    override fun run(args: ApplicationArguments?) {
        log.info { "Bootstrapping existing schedules" }
        scheduleBootstrapUseCase.syncExistingSchedules()
    }
}
