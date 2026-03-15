package com.example.scheduler.adapters.out.persistence

import com.example.scheduler.core.application.port.`in`.ScheduleCreateCommand
import com.example.scheduler.core.application.port.`in`.ScheduleUpdateCommand
import com.example.scheduler.core.application.port.out.ScheduleRepositoryPort
import com.example.scheduler.core.domain.model.Schedule
import com.example.scheduler.core.domain.model.ScheduleType
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant

@Repository
class SchedulePersistenceAdapter(
    private val scheduleJpaRepository: ScheduleJpaRepository,
    private val instanceId: String
) : ScheduleRepositoryPort {

    override fun create(command: ScheduleCreateCommand, nextRunAt: Instant?, now: Instant): Schedule {
        val entity = ScheduleEntity(
            id = com.example.scheduler.core.application.service.IdGenerator.newId(),
            name = command.name,
            scheduleType = command.scheduleType,
            cronExpression = command.cronExpression,
            runAt = command.runAt,
            enabled = command.enabled,
            taskId = command.taskId,
            payload = command.payload,
            nextRunAt = nextRunAt,
            lockedBy = null,
            lockedUntil = null,
            createdAt = now,
            updatedAt = now
        )
        return scheduleJpaRepository.save(entity).toDomain()
    }

    override fun update(id: String, command: ScheduleUpdateCommand, nextRunAt: Instant?, now: Instant): Schedule? {
        val existing = scheduleJpaRepository.findById(id).orElse(null) ?: return null
        val updated = existing.copy(
            name = command.name,
            scheduleType = command.scheduleType,
            cronExpression = command.cronExpression,
            runAt = command.runAt,
            enabled = command.enabled,
            taskId = command.taskId,
            payload = command.payload,
            nextRunAt = nextRunAt,
            updatedAt = now
        )
        return scheduleJpaRepository.save(updated).toDomain()
    }

    override fun updateEnabled(id: String, enabled: Boolean, nextRunAt: Instant?, now: Instant): Schedule? {
        val existing = scheduleJpaRepository.findById(id).orElse(null) ?: return null
        val updated = existing.copy(
            enabled = enabled,
            nextRunAt = nextRunAt,
            updatedAt = now
        )
        return scheduleJpaRepository.save(updated).toDomain()
    }

    override fun delete(id: String) {
        scheduleJpaRepository.deleteById(id)
    }

    override fun findById(id: String): Schedule? {
        return scheduleJpaRepository.findById(id).map { it.toDomain() }.orElse(null)
    }

    override fun findAll(): List<Schedule> {
        return scheduleJpaRepository.findAllByOrderByUpdatedAtDesc().map { it.toDomain() }
    }

    override fun listPage(offset: Int, limit: Int): List<Schedule> {
        if (limit <= 0) {
            return emptyList()
        }
        val pageIndex = offset / limit
        val skip = offset % limit
        val pageSize = limit + skip
        val page = scheduleJpaRepository.findPage(PageRequest.of(pageIndex, pageSize))
        return page.drop(skip).take(limit).map { it.toDomain() }
    }

    override fun countAll(): Long = scheduleJpaRepository.count()

    @Transactional
    override fun tryLockSchedule(id: String, now: Instant, lockUntil: Instant): Boolean {
        val updated = scheduleJpaRepository.tryLockSchedule(
            id = id,
            instanceId = instanceId,
            lockUntil = lockUntil,
            now = now
        )
        return updated == 1
    }

    @Transactional
    override fun lockDueSchedules(now: Instant, lockDuration: Duration, limit: Int): List<Schedule> {
        val candidates = scheduleJpaRepository.findDueSchedules(now, PageRequest.of(0, limit))
        if (candidates.isEmpty()) {
            return emptyList()
        }
        val lockUntil = now.plus(lockDuration)
        val locked = mutableListOf<ScheduleEntity>()
        for (candidate in candidates) {
            val updated = scheduleJpaRepository.tryLockSchedule(
                id = candidate.id,
                instanceId = instanceId,
                lockUntil = lockUntil,
                now = now
            )
            if (updated == 1) {
                locked.add(candidate.copy(lockedBy = instanceId, lockedUntil = lockUntil))
            }
        }
        return locked.map { it.toDomain() }
    }

    @Transactional
    override fun releaseLock(id: String) {
        scheduleJpaRepository.releaseLock(id)
    }

    @Transactional
    override fun markRunComplete(id: String, nextRunAt: Instant?, now: Instant, enabled: Boolean) {
        val existing = scheduleJpaRepository.findById(id).orElse(null) ?: return
        val updated = existing.copy(
            nextRunAt = nextRunAt,
            enabled = enabled,
            updatedAt = now,
            lockedBy = null,
            lockedUntil = null
        )
        scheduleJpaRepository.save(updated)
    }
}

private fun ScheduleEntity.toDomain(): Schedule {
    return Schedule(
        id = id,
        name = name,
        scheduleType = scheduleType,
        cronExpression = cronExpression,
        runAt = runAt,
        enabled = enabled,
        taskId = taskId,
        payload = payload,
        nextRunAt = nextRunAt,
        updatedAt = updatedAt
    )
}
