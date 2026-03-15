package com.example.scheduler.adatper.out.persistence

import com.example.scheduler.application.port.ExecutionRepositoryPort
import com.example.scheduler.domain.model.DeliveryExecution
import com.example.scheduler.adatper.out.persistence.entity.toDomain
import com.example.scheduler.adatper.out.persistence.entity.toEntity
import com.example.scheduler.adatper.out.persistence.repository.DeliveryExecutionJpaRepository
import mu.KotlinLogging
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class DeliveryExecutionPersistenceAdapter(
    private val executionRepository: DeliveryExecutionJpaRepository
) : ExecutionRepositoryPort {
    private val log = KotlinLogging.logger {}

    override fun save(execution: DeliveryExecution): DeliveryExecution {
        return try {
            executionRepository.save(execution.toEntity()).toDomain()
        } catch (ex: DataIntegrityViolationException) {
            log.info(
                "Execution already recorded. scheduleId={}, fireTime={}",
                execution.scheduleId,
                execution.fireTime
            )
            execution
        }
    }

    override fun existsByScheduleIdAndFireTime(scheduleId: Long, fireTime: Instant): Boolean {
        return executionRepository.existsByScheduleIdAndFireTime(scheduleId, fireTime)
    }
}
