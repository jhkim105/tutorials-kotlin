package com.example.scheduler.adapters.out.scheduling

import com.example.scheduler.core.application.port.out.SchedulerPort
import com.example.scheduler.core.domain.model.Schedule
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

@Component
class SpringSchedulerAdapter : SchedulerPort {
    override fun schedule(schedule: Schedule) {
        log.debug { "Schedule ${schedule.id} registered for polling" }
    }

    override fun unschedule(scheduleId: String) {
        log.debug { "Schedule $scheduleId unscheduled from polling" }
    }

    override fun reschedule(schedule: Schedule) {
        log.debug { "Schedule ${schedule.id} rescheduled for polling" }
    }
}
