package jhkim105.tutorials.jpadatetime.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.OffsetTime

@Repository
interface TimeRecordRepository : JpaRepository<TimeRecord, String> {
    fun findByOffsetDateTimeBetween(from: OffsetDateTime, to: OffsetDateTime): List<TimeRecord>
    fun findByLocalDateTimeBetween(from: LocalDateTime, to: LocalDateTime): List<TimeRecord>
    fun findByOffsetTimeBetween(from: OffsetTime, to: OffsetTime): List<TimeRecord>
}
