package com.example.scheduler.core.application.port.out

import com.example.scheduler.core.application.port.`in`.ScheduleCreateCommand
import com.example.scheduler.core.application.port.`in`.ScheduleUpdateCommand
import com.example.scheduler.core.domain.model.Schedule
import java.time.Duration
import java.time.Instant

interface ScheduleRepositoryPort {
    fun create(command: ScheduleCreateCommand, nextRunAt: Instant?, now: Instant): Schedule
    fun update(id: String, command: ScheduleUpdateCommand, nextRunAt: Instant?, now: Instant): Schedule?
    fun updateEnabled(id: String, enabled: Boolean, nextRunAt: Instant?, now: Instant): Schedule?
    fun delete(id: String)
    fun findById(id: String): Schedule?
    fun findAll(): List<Schedule>
    fun listPage(offset: Int, limit: Int): List<Schedule>
    fun countAll(): Long
    fun lockDueSchedules(now: Instant, lockDuration: Duration, limit: Int): List<Schedule>
    fun releaseLock(id: String)
    fun markRunComplete(id: String, nextRunAt: Instant?, now: Instant, enabled: Boolean)
}
