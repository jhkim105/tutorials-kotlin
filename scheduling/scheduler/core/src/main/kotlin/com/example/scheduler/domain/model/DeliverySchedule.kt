package com.example.scheduler.domain.model

import java.time.Instant

data class DeliverySchedule(
    var id: Long? = null,

    var name: String,

    var scheduleType: ScheduleType,

    var cronExpression: String? = null,

    var runAt: Instant? = null,

    var enabled: Boolean = true,

    var actionKey: String,

    var payload: String = "{}",

    var timezone: String? = null,

    var updatedAt: Instant = Instant.now()
)
