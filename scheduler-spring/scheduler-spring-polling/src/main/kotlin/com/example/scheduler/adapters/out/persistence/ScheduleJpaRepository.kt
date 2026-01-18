package com.example.scheduler.adapters.out.persistence

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface ScheduleJpaRepository : JpaRepository<ScheduleEntity, String> {
    fun findAllByOrderByUpdatedAtDesc(): List<ScheduleEntity>

    @Query("SELECT s FROM ScheduleEntity s ORDER BY s.updatedAt DESC, s.id DESC")
    fun findPage(pageable: Pageable): List<ScheduleEntity>

    @Query(
        """
        SELECT s FROM ScheduleEntity s
        WHERE s.enabled = true
          AND s.nextRunAt IS NOT NULL
          AND s.nextRunAt <= :now
          AND (s.lockedUntil IS NULL OR s.lockedUntil < :now)
        ORDER BY s.nextRunAt
        """
    )
    fun findDueSchedules(@Param("now") now: Instant, pageable: Pageable): List<ScheduleEntity>

    @Modifying
    @Query(
        """
        UPDATE ScheduleEntity s
        SET s.lockedBy = :instanceId,
            s.lockedUntil = :lockUntil
        WHERE s.id = :id
          AND (s.lockedUntil IS NULL OR s.lockedUntil < :now)
        """
    )
    fun tryLockSchedule(
        @Param("id") id: String,
        @Param("instanceId") instanceId: String,
        @Param("lockUntil") lockUntil: Instant,
        @Param("now") now: Instant
    ): Int

    @Modifying
    @Query(
        """
        UPDATE ScheduleEntity s
        SET s.lockedBy = NULL,
            s.lockedUntil = NULL
        WHERE s.id = :id
        """
    )
    fun releaseLock(@Param("id") id: String): Int
}
