package com.example.scheduler.adapters.out.persistence

import com.example.scheduler.core.application.port.out.ExecutionCreateRequest
import com.example.scheduler.core.application.port.out.ExecutionRepositoryPort
import com.example.scheduler.core.domain.model.Execution
import com.example.scheduler.core.domain.model.ExecutionStatus
import com.example.scheduler.core.domain.model.ExecutionType
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant

@Repository
class ExecutionJpaAdapter(
    private val executionJpaRepository: ExecutionJpaRepository,
    private val instanceId: String
) : ExecutionRepositoryPort {

    override fun create(request: ExecutionCreateRequest, now: Instant): Execution {
        val entity = ExecutionEntity(
            executionId = com.example.scheduler.core.application.service.IdGenerator.newId(),
            scheduleId = request.scheduleId,
            taskId = request.taskId,
            executionType = request.executionType,
            status = ExecutionStatus.PENDING,
            payload = request.payload,
            attemptCount = 0,
            lockedBy = null,
            lockedUntil = null,
            createdAt = now,
            updatedAt = now,
            startedAt = null,
            completedAt = null
        )
        return executionJpaRepository.save(entity).toDomain()
    }

    override fun findById(executionId: String): Execution? {
        return executionJpaRepository.findById(executionId).map { it.toDomain() }.orElse(null)
    }

    override fun findRunningByTaskId(taskId: String): Execution? {
        val list = executionJpaRepository.findRunningByTaskId(
            taskId = taskId,
            status = ExecutionStatus.RUNNING,
            now = Instant.now(),
            pageable = PageRequest.of(0, 1)
        )
        return list.firstOrNull()?.toDomain()
    }

    override fun listRecent(limit: Int): List<Execution> {
        return executionJpaRepository.findRecent(PageRequest.of(0, limit)).map { it.toDomain() }
    }

    override fun findPage(limit: Int): List<Execution> {
        return executionJpaRepository.findRecent(PageRequest.of(0, limit)).map { it.toDomain() }
    }

    override fun findPageAfter(createdAt: Instant, executionId: String, limit: Int): List<Execution> {
        return executionJpaRepository.findPageAfter(createdAt, executionId, PageRequest.of(0, limit)).map { it.toDomain() }
    }

    @Transactional
    override fun lockPendingExecutions(now: Instant, lockDuration: Duration, limit: Int): List<Execution> {
        val candidates = executionJpaRepository.findDueExecutions(
            pending = ExecutionStatus.PENDING,
            running = ExecutionStatus.RUNNING,
            now = now,
            pageable = PageRequest.of(0, limit)
        )
        if (candidates.isEmpty()) {
            return emptyList()
        }
        val lockUntil = now.plus(lockDuration)
        val locked = mutableListOf<ExecutionEntity>()
        for (candidate in candidates) {
            val updated = executionJpaRepository.tryLockExecution(
                executionId = candidate.executionId,
                pending = ExecutionStatus.PENDING,
                running = ExecutionStatus.RUNNING,
                instanceId = instanceId,
                lockUntil = lockUntil,
                now = now
            )
            if (updated == 1) {
                locked.add(
                    candidate.copy(
                        status = ExecutionStatus.RUNNING,
                        lockedBy = instanceId,
                        lockedUntil = lockUntil,
                        attemptCount = candidate.attemptCount + 1,
                        startedAt = candidate.startedAt ?: now,
                        updatedAt = now
                    )
                )
            }
        }
        return locked.map { it.toDomain() }
    }

    override fun markRunning(executionId: String, now: Instant, lockUntil: Instant, attemptCount: Int) {
        executionJpaRepository.tryLockExecution(
            executionId = executionId,
            pending = ExecutionStatus.PENDING,
            running = ExecutionStatus.RUNNING,
            instanceId = instanceId,
            lockUntil = lockUntil,
            now = now
        )
    }

    @Transactional
    override fun markSuccess(executionId: String, now: Instant) {
        updateStatus(executionId, ExecutionStatus.SUCCESS, now)
    }

    @Transactional
    override fun markFailed(executionId: String, now: Instant) {
        updateStatus(executionId, ExecutionStatus.FAILED, now)
    }

    @Transactional
    override fun updateStatus(executionId: String, status: ExecutionStatus, now: Instant) {
        val existing = executionJpaRepository.findById(executionId).orElse(null) ?: return
        val updated = existing.copy(
            status = status,
            updatedAt = now,
            completedAt = if (status == ExecutionStatus.SUCCESS || status == ExecutionStatus.FAILED) {
                now
            } else {
                existing.completedAt
            },
            lockedBy = null,
            lockedUntil = null
        )
        executionJpaRepository.save(updated)
    }
}

private fun ExecutionEntity.toDomain(): Execution {
    return Execution(
        executionId = executionId,
        scheduleId = scheduleId,
        taskId = taskId,
        executionType = executionType,
        status = status,
        payload = payload,
        attemptCount = attemptCount,
        createdAt = createdAt,
        updatedAt = updatedAt,
        startedAt = startedAt,
        completedAt = completedAt
    )
}
