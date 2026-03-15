package com.example.scheduler.core.application.service

import com.example.scheduler.core.application.port.`in`.ScheduleExecutionUseCase
import com.example.scheduler.core.application.port.out.ExecutionCreateRequest
import com.example.scheduler.core.application.port.out.ExecutionRepositoryPort
import com.example.scheduler.core.application.port.out.ScheduleRepositoryPort
import com.example.scheduler.core.application.port.out.TaskRegistryPort
import com.example.scheduler.core.domain.model.ExecutionType
import com.example.scheduler.core.domain.model.Schedule
import com.example.scheduler.core.domain.model.ScheduleType
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Clock
import java.time.Duration

private val log = KotlinLogging.logger {}

class ScheduleExecutionService(
    private val scheduleRepository: ScheduleRepositoryPort,
    private val executionRepository: ExecutionRepositoryPort,
    private val taskRegistry: TaskRegistryPort,
    private val scheduleCalculator: ScheduleCalculator,
    private val clock: Clock,
    private val executionLockSeconds: Long,
    private val scheduleLockSeconds: Long,
    private val batchSize: Int
) : ScheduleExecutionUseCase {

    override fun runDueSchedules() {
        val now = clock.instant()
        val schedules = scheduleRepository.lockDueSchedules(
            now,
            Duration.ofSeconds(scheduleLockSeconds),
            batchSize
        )
        if (schedules.isEmpty()) {
            return
        }
        schedules.forEach { schedule ->
            try {
                executeSchedule(schedule)
            } catch (ex: Exception) {
                log.error(ex) { "Failed to execute schedule ${schedule.id}" }
            }
        }
    }

    private fun executeSchedule(schedule: Schedule) {
        if (!schedule.enabled) {
            scheduleRepository.releaseLock(schedule.id)
            return
        }

        val handler = taskRegistry.get(schedule.taskId) ?: run {
            log.warn { "Missing task handler for ${schedule.taskId}" }
            scheduleRepository.releaseLock(schedule.id)
            return
        }

        val now = clock.instant()
        val lockUntil = now.plus(Duration.ofSeconds(executionLockSeconds))
        val execution = try {
            executionRepository.createRunning(
                ExecutionCreateRequest(
                    scheduleId = schedule.id,
                    taskId = schedule.taskId,
                    executionType = ExecutionType.SCHEDULE,
                    payload = schedule.payload
                ),
                now,
                lockUntil
            )
        } catch (ex: Exception) {
            log.error(ex) { "Failed to create running execution for schedule ${schedule.id}" }
            scheduleRepository.releaseLock(schedule.id)
            return
        }

        try {
            handler.execute(execution.payload)
            executionRepository.markSuccess(execution.executionId, clock.instant())
        } catch (ex: Exception) {
            log.error(ex) { "Execution failed ${execution.executionId}" }
            executionRepository.markFailed(execution.executionId, clock.instant())
        } finally {
            val finishedAt = clock.instant()
            val nextRunAt = if (schedule.scheduleType == ScheduleType.CRON) {
                scheduleCalculator.nextRunAt(
                    schedule.scheduleType,
                    schedule.cronExpression,
                    schedule.runAt,
                    finishedAt
                )
            } else {
                null
            }
            val enabled = schedule.scheduleType != ScheduleType.ONCE && schedule.enabled
            scheduleRepository.markRunComplete(schedule.id, nextRunAt, finishedAt, enabled)
        }
    }
}
