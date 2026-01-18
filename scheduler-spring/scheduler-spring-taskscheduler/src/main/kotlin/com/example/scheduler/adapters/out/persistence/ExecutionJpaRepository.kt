package com.example.scheduler.adapters.out.persistence

import com.example.scheduler.core.domain.model.ExecutionStatus
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface ExecutionJpaRepository : JpaRepository<ExecutionEntity, String> {
    @Query(
        """
        SELECT e FROM ExecutionEntity e
        WHERE e.taskId = :taskId
          AND e.status = :status
          AND (e.lockedUntil IS NULL OR e.lockedUntil >= :now)
        ORDER BY e.updatedAt DESC
        """
    )
    fun findRunningByTaskId(
        @Param("taskId") taskId: String,
        @Param("status") status: ExecutionStatus,
        @Param("now") now: Instant,
        pageable: Pageable
    ): List<ExecutionEntity>

    @Query("SELECT e FROM ExecutionEntity e ORDER BY e.createdAt DESC, e.executionId DESC")
    fun findRecent(pageable: Pageable): List<ExecutionEntity>

    @Query(
        """
        SELECT e FROM ExecutionEntity e
        WHERE e.createdAt < :createdAt
           OR (e.createdAt = :createdAt AND e.executionId < :executionId)
        ORDER BY e.createdAt DESC, e.executionId DESC
        """
    )
    fun findPageAfter(
        @Param("createdAt") createdAt: Instant,
        @Param("executionId") executionId: String,
        pageable: Pageable
    ): List<ExecutionEntity>

    @Query(
        """
        SELECT e FROM ExecutionEntity e
        WHERE e.status = :pending
           OR (e.status = :running AND e.lockedUntil < :now)
        ORDER BY e.createdAt
        """
    )
    fun findDueExecutions(
        @Param("pending") pending: ExecutionStatus,
        @Param("running") running: ExecutionStatus,
        @Param("now") now: Instant,
        pageable: Pageable
    ): List<ExecutionEntity>

    @Modifying
    @Query(
        """
        UPDATE ExecutionEntity e
        SET e.status = :running,
            e.lockedBy = :instanceId,
            e.lockedUntil = :lockUntil,
            e.attemptCount = e.attemptCount + 1,
            e.startedAt = COALESCE(e.startedAt, :now),
            e.updatedAt = :now
        WHERE e.executionId = :executionId
          AND (e.status = :pending OR (e.status = :running AND e.lockedUntil < :now))
        """
    )
    fun tryLockExecution(
        @Param("executionId") executionId: String,
        @Param("pending") pending: ExecutionStatus,
        @Param("running") running: ExecutionStatus,
        @Param("instanceId") instanceId: String,
        @Param("lockUntil") lockUntil: Instant,
        @Param("now") now: Instant
    ): Int

}
