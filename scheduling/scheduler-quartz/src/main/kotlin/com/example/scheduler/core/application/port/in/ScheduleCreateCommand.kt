package com.example.scheduler.core.application.port.`in`

import com.example.scheduler.core.domain.model.ScheduleType
import java.time.Instant

data class ScheduleCreateCommand(
    val name: String,
    val scheduleType: ScheduleType,
    val cronExpression: String?,
    val runAt: Instant?,
    val enabled: Boolean,
    val taskId: String,
    val payload: String?
)
