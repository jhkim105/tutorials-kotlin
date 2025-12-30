package com.example.scheduler.infra.persistence.repository

import com.example.scheduler.infra.persistence.entity.DeliveryScheduleEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant

interface DeliveryScheduleJpaRepository : JpaRepository<DeliveryScheduleEntity, Long> {
    fun findAllByEnabledTrue(): List<DeliveryScheduleEntity>

    fun findAllByUpdatedAtAfter(updatedAt: Instant): List<DeliveryScheduleEntity>
}
