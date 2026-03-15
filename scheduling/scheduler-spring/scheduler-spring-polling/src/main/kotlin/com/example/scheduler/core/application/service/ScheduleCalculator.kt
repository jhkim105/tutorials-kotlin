package com.example.scheduler.core.application.service

import com.example.scheduler.core.domain.model.ScheduleType
import java.time.Instant
import java.time.ZoneId
import org.springframework.scheduling.support.CronExpression

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
                    val cron = CronExpression.parse(cronExpression)
                    val next = cron.next(now.atZone(ZoneId.systemDefault()))
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
