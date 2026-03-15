package com.example.scheduler.adapters.`in`.web

import com.example.scheduler.core.domain.model.ScheduleType
import java.time.Instant

data class ScheduleRequest(
    val name: String,
    val scheduleType: ScheduleType,
    val cronExpression: String?,
    val runAt: Instant?,
    val enabled: Boolean,
    val actionKey: String,
    val payload: String?
)
