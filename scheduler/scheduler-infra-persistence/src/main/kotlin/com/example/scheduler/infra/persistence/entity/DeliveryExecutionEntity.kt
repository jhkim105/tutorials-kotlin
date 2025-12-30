package com.example.scheduler.infra.persistence.entity

import com.example.scheduler.domain.model.DeliveryExecution
import com.example.scheduler.domain.model.ExecutionStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant

@Entity
@Table(
    name = "delivery_execution",
    uniqueConstraints = [
        UniqueConstraint(
            name = "ux_execution_schedule_fire",
            columnNames = ["schedule_id", "fire_time"]
        )
    ]
)
data class DeliveryExecutionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "schedule_id", nullable = false)
    var scheduleId: Long,

    @Column(name = "fire_time", nullable = false)
    var fireTime: Instant,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ExecutionStatus,

    @Column
    var errorMessage: String? = null
)

fun DeliveryExecutionEntity.toDomain(): DeliveryExecution {
    return DeliveryExecution(
        id = id,
        scheduleId = scheduleId,
        fireTime = fireTime,
        status = status,
        errorMessage = errorMessage
    )
}

fun DeliveryExecution.toEntity(): DeliveryExecutionEntity {
    return DeliveryExecutionEntity(
        id = id,
        scheduleId = scheduleId,
        fireTime = fireTime,
        status = status,
        errorMessage = errorMessage
    )
}
