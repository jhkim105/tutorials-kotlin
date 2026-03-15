package com.example.scheduler.application.service

import com.example.scheduler.application.port.ActionResolver
import com.example.scheduler.application.port.ExecutionRepositoryPort
import com.example.scheduler.application.port.ExecutionUseCase
import com.example.scheduler.application.port.ScheduleRepositoryPort
import com.example.scheduler.domain.model.DeliveryExecution
import com.example.scheduler.domain.model.ExecutionStatus
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class ExecutionService(
    private val scheduleRepository: ScheduleRepositoryPort,
    private val executionRepository: ExecutionRepositoryPort,
    private val actionResolver: ActionResolver
) : ExecutionUseCase {
    private val log = KotlinLogging.logger {}

    override fun execute(scheduleId: Long, fireTime: Instant) {
        if (executionRepository.existsByScheduleIdAndFireTime(scheduleId, fireTime)) {
            log.info("Execution skipped (duplicate). scheduleId={}, fireTime={}", scheduleId, fireTime)
            return
        }

        val schedule = scheduleRepository.findById(scheduleId)
        if (schedule == null) {
            log.warn("Schedule not found. scheduleId={}", scheduleId)
            return
        }

        if (!schedule.enabled) {
            log.info("Schedule disabled. scheduleId={}", scheduleId)
            return
        }

        var status = ExecutionStatus.SUCCESS
        var errorMessage: String? = null

        try {
            val handler = actionResolver.resolve(schedule.actionKey)
            handler.execute(schedule.payload, schedule)
        } catch (ex: Exception) {
            status = ExecutionStatus.FAILED
            errorMessage = ex.message
            log.error("Execution failed. scheduleId={}, message={}", scheduleId, ex.message, ex)
        }

        executionRepository.save(
            DeliveryExecution(
                scheduleId = scheduleId,
                fireTime = fireTime,
                status = status,
                errorMessage = errorMessage
            )
        )
    }
}
