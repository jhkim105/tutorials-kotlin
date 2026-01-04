package com.example.scheduler.adatper.out.persistence.repository

import com.example.scheduler.adatper.out.persistence.entity.DeliveryScheduleEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant

interface DeliveryScheduleJpaRepository : JpaRepository<DeliveryScheduleEntity, Long> {
    fun findAllByEnabledTrue(): List<DeliveryScheduleEntity>

    fun findAllByUpdatedAtAfter(updatedAt: Instant): List<DeliveryScheduleEntity>
}
