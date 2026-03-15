package com.example.scheduler.domain.model

import java.time.Instant

data class DeliveryExecution(
    var id: Long? = null,

    var scheduleId: Long,

    var fireTime: Instant,

    var status: ExecutionStatus,

    var errorMessage: String? = null
)
