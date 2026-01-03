package com.example.scheduler.application.port

import com.example.scheduler.domain.model.DeliverySchedule

interface ScheduleRefresher {
    fun refreshSchedule(schedule: DeliverySchedule)
}
