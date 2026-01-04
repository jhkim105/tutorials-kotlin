package com.example.scheduler.adatper.out.persistence

import com.example.scheduler.application.port.ScheduleRepositoryPort
import com.example.scheduler.domain.model.DeliverySchedule
import com.example.scheduler.adatper.out.persistence.entity.toDomain
import com.example.scheduler.adatper.out.persistence.entity.toEntity
import com.example.scheduler.adatper.out.persistence.repository.DeliveryScheduleJpaRepository
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class DeliverySchedulePersistenceAdapter(
    private val scheduleRepository: DeliveryScheduleJpaRepository
) : ScheduleRepositoryPort {
    override fun save(schedule: DeliverySchedule): DeliverySchedule {
        return scheduleRepository.save(schedule.toEntity()).toDomain()
    }

    override fun findById(id: Long): DeliverySchedule? {
        return scheduleRepository.findById(id).map { it.toDomain() }.orElse(null)
    }

    override fun findAll(): List<DeliverySchedule> {
        return scheduleRepository.findAll().map { it.toDomain() }
    }

    override fun findAllByEnabledTrue(): List<DeliverySchedule> {
        return scheduleRepository.findAllByEnabledTrue().map { it.toDomain() }
    }

    override fun findAllByUpdatedAtAfter(updatedAt: Instant): List<DeliverySchedule> {
        return scheduleRepository.findAllByUpdatedAtAfter(updatedAt).map { it.toDomain() }
    }
}
