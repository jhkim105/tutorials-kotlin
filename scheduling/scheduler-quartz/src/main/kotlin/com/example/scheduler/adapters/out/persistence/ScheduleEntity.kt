package com.example.scheduler.adapters.out.persistence

import com.example.scheduler.core.domain.model.ScheduleType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "schedules")
data class ScheduleEntity(
    @Id
    @Column(name = "id", length = 36)
    val id: String,

    @Column(name = "name", nullable = false, length = 200)
    val name: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_type", nullable = false, length = 10)
    val scheduleType: ScheduleType,

    @Column(name = "cron_expression", length = 120)
    val cronExpression: String?,

    @Column(name = "run_at")
    val runAt: Instant?,

    @Column(name = "enabled", nullable = false)
    val enabled: Boolean,

    @Column(name = "task_id", nullable = false, length = 100)
    val taskId: String,

    @Column(name = "payload", columnDefinition = "TEXT")
    val payload: String?,

    @Column(name = "next_run_at")
    val nextRunAt: Instant?,

    @Column(name = "locked_by", length = 64)
    val lockedBy: String?,

    @Column(name = "locked_until")
    val lockedUntil: Instant?,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,

    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant
)
