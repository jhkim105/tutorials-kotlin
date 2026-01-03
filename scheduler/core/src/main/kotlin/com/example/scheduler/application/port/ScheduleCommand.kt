package com.example.scheduler.application.port

import com.example.scheduler.domain.model.ScheduleType
import java.time.Instant

data class ScheduleCommand(
    val name: String,
    val scheduleType: ScheduleType,
    val cronExpression: String? = null,
    val runAt: Instant? = null,
    val enabled: Boolean = true,
    val actionKey: String,
    val payload: String? = null,
    val timezone: String? = null
)
