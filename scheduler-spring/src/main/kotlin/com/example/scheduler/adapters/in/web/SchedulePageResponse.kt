package com.example.scheduler.adapters.`in`.web

data class SchedulePageResponse(
    val items: List<ScheduleResponse>,
    val total: Long,
    val limit: Int,
    val offset: Int
)
