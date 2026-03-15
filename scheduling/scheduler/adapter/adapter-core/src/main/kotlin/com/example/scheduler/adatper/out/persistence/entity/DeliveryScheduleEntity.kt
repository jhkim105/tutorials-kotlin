package com.example.scheduler.adatper.out.persistence.entity

import com.example.scheduler.domain.model.DeliverySchedule
import com.example.scheduler.domain.model.ScheduleType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Lob
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "delivery_schedule")
data class DeliveryScheduleEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var name: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var scheduleType: ScheduleType,

    @Column
    var cronExpression: String? = null,

    @Column
    var runAt: Instant? = null,

    @Column(nullable = false)
    var enabled: Boolean = true,

    @Column(nullable = false)
    var actionKey: String,

    @Lob
    @Column(nullable = false)
    var payload: String = "{}",

    @Column
    var timezone: String? = null,

    @Column(nullable = false)
    var updatedAt: Instant = Instant.now()
)

fun DeliveryScheduleEntity.toDomain(): DeliverySchedule {
    return DeliverySchedule(
        id = id,
        name = name,
        scheduleType = scheduleType,
        cronExpression = cronExpression,
        runAt = runAt,
        enabled = enabled,
        actionKey = actionKey,
        payload = payload,
        timezone = timezone,
        updatedAt = updatedAt
    )
}

fun DeliverySchedule.toEntity(): DeliveryScheduleEntity {
    return DeliveryScheduleEntity(
        id = id,
        name = name,
        scheduleType = scheduleType,
        cronExpression = cronExpression,
        runAt = runAt,
        enabled = enabled,
        actionKey = actionKey,
        payload = payload,
        timezone = timezone,
        updatedAt = updatedAt
    )
}
