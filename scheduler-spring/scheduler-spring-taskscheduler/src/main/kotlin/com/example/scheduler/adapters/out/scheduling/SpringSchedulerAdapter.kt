package com.example.scheduler.adapters.out.scheduling

import com.example.scheduler.core.application.port.`in`.ScheduleExecutionUseCase
import com.example.scheduler.core.application.port.out.SchedulerPort
import com.example.scheduler.core.domain.model.Schedule
import com.example.scheduler.core.domain.model.ScheduleType
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledFuture
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.support.CronTrigger
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

@Component
class SpringSchedulerAdapter(
        private val taskScheduler: TaskScheduler,
        private val scheduleExecutionUseCase: ScheduleExecutionUseCase
) : SchedulerPort {
    private val scheduledTasks = ConcurrentHashMap<String, ScheduledFuture<*>>()

    override fun schedule(schedule: Schedule) {
        unschedule(schedule.id)

        if (!schedule.enabled) {
            return
        }

        val runnable = Runnable {
            try {
                scheduleExecutionUseCase.runSchedule(schedule.id)
            } catch (ex: Exception) {
                log.error(ex) { "Error executing schedule ${schedule.id}" }
            }
        }

        val future =
                try {
                    if (schedule.scheduleType == ScheduleType.CRON &&
                                    schedule.cronExpression != null
                    ) {
                        taskScheduler.schedule(runnable, CronTrigger(schedule.cronExpression))
                    } else if (schedule.scheduleType == ScheduleType.ONCE && schedule.runAt != null
                    ) {
                        taskScheduler.schedule(runnable, schedule.runAt)
                    } else {
                        log.warn { "Unsupported schedule type or missing data: $schedule" }
                        null
                    }
                } catch (ex: Exception) {
                    log.error(ex) { "Failed to schedule ${schedule.id}" }
                    null
                }

        if (future != null) {
            scheduledTasks[schedule.id] = future
            log.trace { "Scheduled ${schedule.id}" }
        }
    }

    override fun unschedule(scheduleId: String) {
        scheduledTasks.remove(scheduleId)?.cancel(false)
        log.trace { "Unscheduled $scheduleId" }
    }

    override fun reschedule(schedule: Schedule) {
        schedule(schedule)
    }
}
