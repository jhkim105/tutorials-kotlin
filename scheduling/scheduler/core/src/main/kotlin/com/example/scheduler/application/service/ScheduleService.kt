package com.example.scheduler.application.service

import com.example.scheduler.application.port.ScheduleCommand
import com.example.scheduler.application.port.ScheduleRefresher
import com.example.scheduler.application.port.ScheduleRepositoryPort
import com.example.scheduler.application.port.ScheduleUseCase
import com.example.scheduler.domain.model.DeliverySchedule
import com.example.scheduler.domain.model.ScheduleType
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

@Service
class ScheduleService(
    private val scheduleRepository: ScheduleRepositoryPort,
    private val scheduleRefresher: ScheduleRefresher
) : ScheduleUseCase {
    override fun create(command: ScheduleCommand): DeliverySchedule {
        validateRequest(command)
        val schedule = DeliverySchedule(
            name = command.name,
            scheduleType = command.scheduleType,
            cronExpression = command.cronExpression,
            runAt = command.runAt,
            enabled = command.enabled,
            actionKey = command.actionKey,
            payload = normalizedPayload(command.payload),
            timezone = command.timezone,
            updatedAt = Instant.now()
        )

        val saved = scheduleRepository.save(schedule)
        scheduleRefresher.refreshSchedule(saved)
        return saved
    }

    override fun update(id: Long, command: ScheduleCommand): DeliverySchedule {
        validateRequest(command)
        val schedule = scheduleRepository.findById(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule not found")

        schedule.name = command.name
        schedule.scheduleType = command.scheduleType
        schedule.cronExpression = command.cronExpression
        schedule.runAt = command.runAt
        schedule.enabled = command.enabled
        schedule.actionKey = command.actionKey
        schedule.payload = normalizedPayload(command.payload)
        schedule.timezone = command.timezone
        schedule.updatedAt = Instant.now()

        val saved = scheduleRepository.save(schedule)
        scheduleRefresher.refreshSchedule(saved)
        return saved
    }

    override fun enable(id: Long): DeliverySchedule {
        val schedule = scheduleRepository.findById(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule not found")

        schedule.enabled = true
        schedule.updatedAt = Instant.now()
        val saved = scheduleRepository.save(schedule)
        scheduleRefresher.refreshSchedule(saved)
        return saved
    }

    override fun disable(id: Long): DeliverySchedule {
        val schedule = scheduleRepository.findById(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule not found")

        schedule.enabled = false
        schedule.updatedAt = Instant.now()
        val saved = scheduleRepository.save(schedule)
        scheduleRefresher.refreshSchedule(saved)
        return saved
    }

    override fun list(): List<DeliverySchedule> = scheduleRepository.findAll()

    private fun validateRequest(command: ScheduleCommand) {
        when (command.scheduleType) {
            ScheduleType.CRON -> {
                if (command.cronExpression.isNullOrBlank()) {
                    throw ResponseStatusException(HttpStatus.BAD_REQUEST, "cronExpression is required for CRON")
                }
            }
            ScheduleType.ONCE -> {
                if (command.runAt == null) {
                    throw ResponseStatusException(HttpStatus.BAD_REQUEST, "runAt is required for ONCE")
                }
            }
        }
    }

    private fun normalizedPayload(payload: String?): String {
        if (payload.isNullOrBlank()) {
            return "{}"
        }
        return payload
    }
}
