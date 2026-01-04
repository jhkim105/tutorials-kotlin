package com.example.scheduler.adatper.out.persistence.repository

import com.example.scheduler.adatper.out.persistence.entity.DeliveryExecutionEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant

interface DeliveryExecutionJpaRepository : JpaRepository<DeliveryExecutionEntity, Long> {
    fun existsByScheduleIdAndFireTime(scheduleId: Long, fireTime: Instant): Boolean
}
