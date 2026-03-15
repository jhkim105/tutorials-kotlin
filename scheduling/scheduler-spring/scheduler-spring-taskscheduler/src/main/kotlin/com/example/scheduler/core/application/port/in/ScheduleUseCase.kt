package com.example.scheduler.core.application.port.`in`

import com.example.scheduler.core.domain.model.Schedule

interface ScheduleUseCase {
    fun create(command: ScheduleCreateCommand): Schedule
    fun update(id: String, command: ScheduleUpdateCommand): Schedule
    fun enable(id: String): Schedule
    fun disable(id: String): Schedule
    fun delete(id: String)
    fun get(id: String): Schedule
    fun list(): List<Schedule>
    fun listPage(offset: Int, limit: Int): SchedulePage
}
