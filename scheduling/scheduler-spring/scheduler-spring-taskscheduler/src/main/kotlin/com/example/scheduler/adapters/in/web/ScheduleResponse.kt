package com.example.scheduler.adapters.`in`.web

import com.example.scheduler.core.domain.model.Schedule
import com.example.scheduler.core.domain.model.ScheduleType
import java.time.Instant

data class ScheduleResponse(
    val id: String,
    val name: String,
    val scheduleType: ScheduleType,
    val cronExpression: String?,
    val runAt: Instant?,
    val enabled: Boolean,
    val actionKey: String,
    val payload: String?,
    val nextRunAt: Instant?,
    val updatedAt: Instant
) {
    companion object {
        fun from(schedule: Schedule): ScheduleResponse {
            return ScheduleResponse(
                id = schedule.id,
                name = schedule.name,
                scheduleType = schedule.scheduleType,
                cronExpression = schedule.cronExpression,
                runAt = schedule.runAt,
                enabled = schedule.enabled,
                actionKey = schedule.taskId,
                payload = schedule.payload,
                nextRunAt = schedule.nextRunAt,
                updatedAt = schedule.updatedAt
            )
        }
    }
}
