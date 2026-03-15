package com.example.scheduler.core.application.service

import com.example.scheduler.core.domain.model.ScheduleType
import org.quartz.CronExpression
import java.time.Instant

class ScheduleCalculator {
    fun nextRunAt(
        scheduleType: ScheduleType,
        cronExpression: String?,
        runAt: Instant?,
        now: Instant
    ): Instant? {
        return when (scheduleType) {
            ScheduleType.CRON -> {
                if (cronExpression.isNullOrBlank()) {
                    throw ValidationException("cronExpression is required for CRON schedule")
                }
                try {
                    val cron = CronExpression(cronExpression)
                    val next = cron.getNextValidTimeAfter(java.util.Date.from(now))
                    next?.toInstant()
                } catch (ex: Exception) {
                    throw ValidationException("Invalid cronExpression: $cronExpression")
                }
            }
            ScheduleType.ONCE -> {
                if (runAt == null) {
                    throw ValidationException("runAt is required for ONCE schedule")
                }
                runAt
            }
        }
    }
}
