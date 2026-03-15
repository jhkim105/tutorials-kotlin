package com.example.scheduler.core.domain.model

import java.time.Instant

data class Schedule(
    val id: String,
    val name: String,
    val scheduleType: ScheduleType,
    val cronExpression: String?,
    val runAt: Instant?,
    val enabled: Boolean,
    val taskId: String,
    val payload: String?,
    val nextRunAt: Instant?,
    val updatedAt: Instant
)
