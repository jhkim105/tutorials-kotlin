package com.example.scheduler.adapters.out.quartz

import com.example.scheduler.core.application.port.out.ExecutionCreateRequest
import com.example.scheduler.core.application.port.out.ExecutionRepositoryPort
import com.example.scheduler.core.application.port.out.ScheduleRepositoryPort
import com.example.scheduler.core.application.port.out.TaskRegistryPort
import com.example.scheduler.core.application.service.ScheduleCalculator
import com.example.scheduler.core.domain.model.ExecutionType
import com.example.scheduler.core.domain.model.ScheduleType
import io.github.oshai.kotlinlogging.KotlinLogging
import org.quartz.DisallowConcurrentExecution
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.Duration

private val log = KotlinLogging.logger {}

@DisallowConcurrentExecution
@Component
class TaskExecutionJob(
    private val scheduleRepository: ScheduleRepositoryPort,
    private val executionRepository: ExecutionRepositoryPort,
    private val taskRegistry: TaskRegistryPort,
    private val scheduleCalculator: ScheduleCalculator,
    private val clock: Clock,
    @Value("\${scheduler.lock.execution-seconds:300}") private val executionLockSeconds: Long,
    @Value("\${scheduler.lock.schedule-seconds:30}") private val scheduleLockSeconds: Long
) : Job {

    override fun execute(context: JobExecutionContext) {
        val scheduleId = context.trigger.key.name.removePrefix(QuartzSchedulerAdapter.SCHEDULE_TRIGGER_PREFIX)
        val taskId = context.mergedJobDataMap.getString(QuartzSchedulerAdapter.JOB_TASK_ID_KEY)
        val schedule = scheduleRepository.findById(scheduleId) ?: return
        if (!schedule.enabled) {
            return
        }
        if (taskId != schedule.taskId) {
            log.warn { "Task mismatch for schedule $scheduleId: job=$taskId, schedule=${schedule.taskId}" }
        }
        val now = clock.instant()
        val scheduleLockUntil = now.plus(Duration.ofSeconds(scheduleLockSeconds))
        val scheduleLocked = scheduleRepository.tryLockSchedule(schedule.id, now, scheduleLockUntil)
        if (!scheduleLocked) {
            return
        }

        val handler = taskRegistry.get(schedule.taskId) ?: run {
            log.warn { "Missing task handler for ${schedule.taskId}" }
            scheduleRepository.releaseLock(schedule.id)
            return
        }

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
