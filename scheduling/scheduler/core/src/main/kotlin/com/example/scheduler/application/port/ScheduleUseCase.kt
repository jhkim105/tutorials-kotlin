package com.example.scheduler.application.port

import com.example.scheduler.domain.model.DeliverySchedule

interface ScheduleUseCase {
    fun create(command: ScheduleCommand): DeliverySchedule

    fun update(id: Long, command: ScheduleCommand): DeliverySchedule

    fun enable(id: Long): DeliverySchedule

    fun disable(id: Long): DeliverySchedule

    fun list(): List<DeliverySchedule>
}
