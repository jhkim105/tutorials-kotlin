package com.example.scheduler.core.application.service

import com.example.scheduler.core.application.port.`in`.ScheduleCreateCommand
import com.example.scheduler.core.application.port.`in`.SchedulePage
import com.example.scheduler.core.application.port.`in`.ScheduleUpdateCommand
import com.example.scheduler.core.application.port.`in`.ScheduleUseCase
import com.example.scheduler.core.application.port.out.ScheduleRepositoryPort
import com.example.scheduler.core.application.port.out.SchedulerPort
import com.example.scheduler.core.application.port.out.TaskRegistryPort
import com.example.scheduler.core.domain.model.Schedule
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Clock

private val log = KotlinLogging.logger {}

class ScheduleService(
    private val scheduleRepository: ScheduleRepositoryPort,
    private val taskRegistry: TaskRegistryPort,
    private val schedulerPort: SchedulerPort,
    private val clock: Clock,
    private val calculator: ScheduleCalculator
) : ScheduleUseCase {
    override fun create(command: ScheduleCreateCommand): Schedule {
        validateTask(command.taskId)
        val now = clock.instant()
        val nextRunAt = if (command.enabled) {
            calculator.nextRunAt(command.scheduleType, command.cronExpression, command.runAt, now)
        } else {
            null
        }
        val schedule = scheduleRepository.create(command, nextRunAt, now)
        if (schedule.enabled) {
            schedulerPort.schedule(schedule)
        }
        return schedule
    }

    override fun update(id: String, command: ScheduleUpdateCommand): Schedule {
        validateTask(command.taskId)
        val now = clock.instant()
        val nextRunAt = if (command.enabled) {
            calculator.nextRunAt(command.scheduleType, command.cronExpression, command.runAt, now)
        } else {
            null
        }
        val updated = scheduleRepository.update(id, command, nextRunAt, now)
            ?: throw NotFoundException("Schedule not found: $id")
        if (updated.enabled) {
            schedulerPort.reschedule(updated)
        } else {
            schedulerPort.unschedule(updated.id)
        }
        return updated
    }

    override fun enable(id: String): Schedule {
        val schedule = scheduleRepository.findById(id) ?: throw NotFoundException("Schedule not found: $id")
        val now = clock.instant()
        val nextRunAt = calculator.nextRunAt(schedule.scheduleType, schedule.cronExpression, schedule.runAt, now)
        val updated = scheduleRepository.updateEnabled(id, true, nextRunAt, now)
            ?: throw NotFoundException("Schedule not found: $id")
        schedulerPort.schedule(updated)
        return updated
    }

    override fun disable(id: String): Schedule {
        val now = clock.instant()
        val updated = scheduleRepository.updateEnabled(id, false, null, now)
            ?: throw NotFoundException("Schedule not found: $id")
        schedulerPort.unschedule(updated.id)
        return updated
    }

    override fun delete(id: String) {
        scheduleRepository.delete(id)
        schedulerPort.unschedule(id)
    }

    override fun get(id: String): Schedule {
        return scheduleRepository.findById(id) ?: throw NotFoundException("Schedule not found: $id")
    }

    override fun list(): List<Schedule> = scheduleRepository.findAll()

    override fun listPage(offset: Int, limit: Int): SchedulePage {
        val safeLimit = limit.coerceIn(1, 100)
        val safeOffset = offset.coerceAtLeast(0)
        val total = scheduleRepository.countAll()
        val items = scheduleRepository.listPage(safeOffset, safeLimit)
        return SchedulePage(items, total, safeLimit, safeOffset)
    }

    private fun validateTask(taskId: String) {
        if (taskRegistry.get(taskId) == null) {
            throw ValidationException("Unknown taskId: $taskId")
        }
    }
}
