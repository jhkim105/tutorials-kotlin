package jhkim105.tutorials.jpadatetime.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.ZonedDateTime
import java.util.UUID

@Entity
@Table(name = "time_record")
class TimeRecord(
    @Id
    @Column(name = "id", nullable = false, length = 36)
    var id: String = UUID.randomUUID().toString(),

    @Column(name = "title", nullable = false)
    var title: String,

    @Column(name = "local_date_time", nullable = false)
    var localDateTime: LocalDateTime,

    @Column(name = "local_date", nullable = false)
    var localDate: LocalDate,

    @Column(name = "local_time", nullable = false)
    var localTime: LocalTime,

    @Column(name = "instant", nullable = false)
    var instant: Instant,

    @Column(name = "zoned_date_time", nullable = false)
    var zonedDateTime: ZonedDateTime,

    @Column(name = "offset_date_time", nullable = false)
    var offsetDateTime: OffsetDateTime,

    @Column(name = "offset_time", nullable = false)
    var offsetTime: OffsetTime,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
) {
    @PrePersist
    fun onPrePersist() {
        val now = Instant.now()
        if (createdAt == Instant.EPOCH) {
            createdAt = now
        }
        updatedAt = now
    }

    @PreUpdate
    fun onPreUpdate() {
        updatedAt = Instant.now()
    }
}
