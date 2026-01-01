package com.example.scheduler.core.application.service

import com.example.scheduler.core.application.port.out.ExecutionCreateRequest
import com.example.scheduler.core.application.port.out.ExecutionRepositoryPort
import com.example.scheduler.core.application.port.out.ScheduleRepositoryPort
import com.example.scheduler.core.application.port.out.TaskRegistryPort
import com.example.scheduler.core.domain.model.ExecutionType
import com.example.scheduler.core.domain.model.ScheduleType
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Clock
import java.time.Duration
import java.time.Instant

private val log = KotlinLogging.logger {}

class ExecutionCoordinator(
    private val scheduleRepository: ScheduleRepositoryPort,
    private val executionRepository: ExecutionRepositoryPort,
    private val taskRegistry: TaskRegistryPort,
    private val calculator: ScheduleCalculator,
    private val clock: Clock,
    private val instanceId: String,
    private val scheduleLockDuration: Duration,
    private val executionLockDuration: Duration,
    private val batchSize: Int
) {
    fun triggerDueSchedules() {
        val now = clock.instant()
        val dueSchedules = scheduleRepository.lockDueSchedules(now, scheduleLockDuration, batchSize)
        if (dueSchedules.isEmpty()) {
            return
        }
        dueSchedules.forEach { schedule ->
            try {
                val task = taskRegistry.get(schedule.taskId)
                if (task == null) {
                    log.warn { "Skipping schedule ${schedule.id} due to missing task ${schedule.taskId}" }
                    scheduleRepository.releaseLock(schedule.id)
                    return@forEach
                }
                executionRepository.create(
                    ExecutionCreateRequest(
                        scheduleId = schedule.id,
                        taskId = schedule.taskId,
                        executionType = ExecutionType.SCHEDULE,
                        payload = schedule.payload
                    ),
                    now
                )
                val nextRunAt = if (schedule.enabled) {
                    calculator.nextRunAt(schedule.scheduleType, schedule.cronExpression, schedule.runAt, now)
                } else {
                    null
                }
                val enabled = if (schedule.scheduleType == ScheduleType.ONCE) {
                    false
                } else {
                    schedule.enabled
                }
                val finalNext = if (schedule.scheduleType == ScheduleType.ONCE) {
                    null
                } else {
                    nextRunAt
                }
                scheduleRepository.markRunComplete(schedule.id, finalNext, now, enabled)
            } catch (ex: Exception) {
                log.error(ex) { "Failed to trigger schedule ${schedule.id}" }
                scheduleRepository.releaseLock(schedule.id)
            }
        }
    }

    fun runPendingExecutions() {
        val now = clock.instant()
        val pending = executionRepository.lockPendingExecutions(now, executionLockDuration, batchSize)
        if (pending.isEmpty()) {
            return
        }
        pending.forEach { execution ->
            val handler = taskRegistry.get(execution.taskId)
            if (handler == null) {
                log.warn { "Missing task handler for ${execution.taskId}, marking failed" }
                executionRepository.markFailed(execution.executionId, clock.instant())
                return@forEach
            }
            try {
                handler.execute(execution.payload)
                executionRepository.markSuccess(execution.executionId, clock.instant())
            } catch (ex: Exception) {
                log.error(ex) { "Execution failed ${execution.executionId}" }
                executionRepository.markFailed(execution.executionId, clock.instant())
            }
        }
    }

    fun getInstanceId(): String = instanceId
}
