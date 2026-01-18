package com.example.scheduler.core.application.port.out

import com.example.scheduler.core.domain.model.ExecutionType

data class ExecutionCreateRequest(
    val scheduleId: String?,
    val taskId: String,
    val executionType: ExecutionType,
    val payload: String?
)
