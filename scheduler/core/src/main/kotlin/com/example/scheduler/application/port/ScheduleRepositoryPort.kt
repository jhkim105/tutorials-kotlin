package com.example.scheduler.application.port

import com.example.scheduler.domain.model.DeliverySchedule
import java.time.Instant

interface ScheduleRepositoryPort {
    fun save(schedule: DeliverySchedule): DeliverySchedule

    fun findById(id: Long): DeliverySchedule?

    fun findAll(): List<DeliverySchedule>

    fun findAllByEnabledTrue(): List<DeliverySchedule>

    fun findAllByUpdatedAtAfter(updatedAt: Instant): List<DeliverySchedule>
}
