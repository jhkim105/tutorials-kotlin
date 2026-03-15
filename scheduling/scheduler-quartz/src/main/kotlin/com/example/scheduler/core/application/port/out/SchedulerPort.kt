package com.example.scheduler.core.application.port.out

import com.example.scheduler.core.domain.model.Schedule

interface SchedulerPort {
    fun schedule(schedule: Schedule)
    fun unschedule(scheduleId: String)
    fun reschedule(schedule: Schedule)
}
