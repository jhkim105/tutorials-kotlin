package com.example.scheduler.core.application.port.`in`

interface ScheduleExecutionUseCase {
    fun executeScheduled(scheduleId: String, taskId: String?)
}
