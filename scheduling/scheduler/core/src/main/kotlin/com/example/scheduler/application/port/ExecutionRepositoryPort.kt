package com.example.scheduler.application.port

import com.example.scheduler.domain.model.DeliveryExecution
import java.time.Instant

interface ExecutionRepositoryPort {
    fun save(execution: DeliveryExecution): DeliveryExecution

    fun existsByScheduleIdAndFireTime(scheduleId: Long, fireTime: Instant): Boolean
}
