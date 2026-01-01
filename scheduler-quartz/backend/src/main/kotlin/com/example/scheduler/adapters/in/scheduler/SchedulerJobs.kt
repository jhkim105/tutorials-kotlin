package com.example.scheduler.adapters.`in`.scheduler

import com.example.scheduler.core.application.service.ExecutionCoordinator
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class SchedulerJobs(
    private val coordinator: ExecutionCoordinator
) {
    @Scheduled(fixedDelayString = "\${scheduler.trigger-interval-ms:1000}")
    fun triggerSchedules() {
        coordinator.triggerDueSchedules()
    }

    @Scheduled(fixedDelayString = "\${scheduler.execution-interval-ms:1000}")
    fun runExecutions() {
        coordinator.runPendingExecutions()
    }
}
