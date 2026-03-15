package com.example.scheduler.adatper.`in`.api.dto

import com.example.scheduler.application.port.ScheduleCommand
import com.example.scheduler.domain.model.DeliverySchedule
import com.example.scheduler.domain.model.ScheduleType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.Instant

data class ScheduleRequest(
    @field:NotBlank
    val name: String,

    @field:NotNull
    val scheduleType: ScheduleType,

    val cronExpression: String? = null,

    val runAt: Instant? = null,

    val enabled: Boolean = true,

    @field:NotBlank
    val actionKey: String,

    val payload: String? = null,

    val timezone: String? = null
)

data class ScheduleResponse(
    val id: Long,
    val name: String,
    val scheduleType: ScheduleType,
    val cronExpression: String?,
    val runAt: Instant?,
    val enabled: Boolean,
    val actionKey: String,
    val payload: String,
    val timezone: String?,
    val updatedAt: Instant
)

fun DeliverySchedule.toResponse(): ScheduleResponse {
    return ScheduleResponse(
        id = id ?: 0L,
        name = name,
        scheduleType = scheduleType,
        cronExpression = cronExpression,
        runAt = runAt,
        enabled = enabled,
        actionKey = actionKey,
        payload = payload,
        timezone = timezone,
        updatedAt = updatedAt
    )
}

fun ScheduleRequest.toCommand(): ScheduleCommand {
    return ScheduleCommand(
        name = name,
        scheduleType = scheduleType,
        cronExpression = cronExpression,
        runAt = runAt,
        enabled = enabled,
        actionKey = actionKey,
        payload = payload,
        timezone = timezone
    )
}
