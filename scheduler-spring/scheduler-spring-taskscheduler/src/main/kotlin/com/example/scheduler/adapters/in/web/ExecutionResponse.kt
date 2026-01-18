package com.example.scheduler.adapters.`in`.web

import com.example.scheduler.core.domain.model.Execution
import com.example.scheduler.core.domain.model.ExecutionType
import java.time.Instant

data class ExecutionResponse(
    val executionId: String,
    val scheduleId: String?,
    val taskId: String,
    val executionType: ExecutionType,
    val status: String,
    val payload: String?,
    val attemptCount: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
    val startedAt: Instant?,
    val completedAt: Instant?
) {
    companion object {
        fun from(execution: Execution): ExecutionResponse {
            return ExecutionResponse(
                executionId = execution.executionId,
                scheduleId = execution.scheduleId,
                taskId = execution.taskId,
                executionType = execution.executionType,
                status = execution.status.name,
                payload = execution.payload,
                attemptCount = execution.attemptCount,
                createdAt = execution.createdAt,
                updatedAt = execution.updatedAt,
                startedAt = execution.startedAt,
                completedAt = execution.completedAt
            )
        }
    }
}
