package com.example.scheduler.core.application.port.out

import com.example.scheduler.core.domain.model.Execution
import com.example.scheduler.core.domain.model.ExecutionStatus
import java.time.Duration
import java.time.Instant

interface ExecutionRepositoryPort {
    fun create(request: ExecutionCreateRequest, now: Instant): Execution
    fun createRunning(request: ExecutionCreateRequest, now: Instant, lockUntil: Instant): Execution
    fun findById(executionId: String): Execution?
    fun findRunningByTaskId(taskId: String): Execution?
    fun listRecent(limit: Int): List<Execution>
    fun findPage(limit: Int): List<Execution>
    fun findPageAfter(createdAt: Instant, executionId: String, limit: Int): List<Execution>
    fun lockPendingExecutions(now: Instant, lockDuration: Duration, limit: Int): List<Execution>
    fun markRunning(executionId: String, now: Instant, lockUntil: Instant, attemptCount: Int)
    fun markSuccess(executionId: String, now: Instant)
    fun markFailed(executionId: String, now: Instant)
    fun updateStatus(executionId: String, status: ExecutionStatus, now: Instant)
}
