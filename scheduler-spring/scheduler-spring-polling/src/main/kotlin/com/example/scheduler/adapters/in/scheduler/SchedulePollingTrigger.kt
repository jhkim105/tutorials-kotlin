package com.example.scheduler.adapters.`in`.scheduler

import com.example.scheduler.core.application.port.`in`.ScheduleExecutionUseCase
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

@Component
class SchedulePollingTrigger(
    private val scheduleExecutionUseCase: ScheduleExecutionUseCase
) {

    @Scheduled(fixedDelayString = "\${scheduler.poll-interval-ms:1000}")
    fun runPolling() {
        log.trace { "Polling due schedules" }
        scheduleExecutionUseCase.runDueSchedules()
    }
}
