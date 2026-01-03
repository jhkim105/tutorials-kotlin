package com.example.scheduler.core.domain.model

import java.time.Instant

data class Execution(
    val executionId: String,
    val scheduleId: String?,
    val taskId: String,
    val executionType: ExecutionType,
    val status: ExecutionStatus,
    val payload: String?,
    val attemptCount: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
    val startedAt: Instant?,
    val completedAt: Instant?
)
