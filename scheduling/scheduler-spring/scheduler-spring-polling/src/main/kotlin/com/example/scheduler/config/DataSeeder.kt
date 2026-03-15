package com.example.scheduler.config

import com.example.scheduler.core.application.port.`in`.ScheduleCreateCommand
import com.example.scheduler.core.application.port.`in`.ScheduleUseCase
import com.example.scheduler.core.application.port.out.TaskRegistryPort
import com.example.scheduler.core.domain.model.ScheduleType
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

@Component
class DataSeeder(
        private val scheduleUseCase: ScheduleUseCase,
        private val taskRegistry: TaskRegistryPort
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        ensureSchedule(
                taskId = "sampleCleanup",
                scheduleName = "Sample Cleanup Cron",
                scheduleType = ScheduleType.CRON,
                cronExpression = "*/30 * * * * *",
                payload = """{"source":"DataSeeder","type":"cron"}"""
        )
    }

    private fun ensureSchedule(
            taskId: String,
            scheduleName: String,
            scheduleType: ScheduleType,
            cronExpression: String? = null,
            payload: String? = null
    ) {
        if (taskRegistry.get(taskId) == null) {
            log.warn { "Task $taskId not found in registry, skipping schedule seeding." }
            return
        }

        val existingSchedules = scheduleUseCase.list().filter { it.taskId == taskId }
        if (existingSchedules.isNotEmpty()) {
            log.info { "Deleting ${existingSchedules.size} existing schedule(s) for task: $taskId" }
            existingSchedules.forEach { schedule ->
                try {
                    scheduleUseCase.delete(schedule.id)
                } catch (e: Exception) {
                    log.warn(e) { "Failed to delete existing schedule ${schedule.id} for $taskId" }
                }
            }
        }

        log.info { "Seeding schedule for task: $taskId" }
        try {
            scheduleUseCase.create(
                    ScheduleCreateCommand(
                            name = scheduleName,
                            scheduleType = scheduleType,
                            cronExpression = cronExpression,
                            runAt = null,
                            enabled = true,
                            taskId = taskId,
                            payload = payload
                    )
            )
            log.info { "Successfully created schedule for $taskId" }
        } catch (e: Exception) {
            log.error(e) { "Failed to create schedule for $taskId" }
        }
    }
}
