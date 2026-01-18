package com.example.scheduler.core.application.port.`in`

import com.example.scheduler.core.domain.model.Schedule

data class SchedulePage(
    val items: List<Schedule>,
    val total: Long,
    val limit: Int,
    val offset: Int
)
