package jhkim105.tutorials.jpadatetime.dto

import jhkim105.tutorials.jpadatetime.domain.TimeRecord
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.ZoneId
import java.time.ZonedDateTime

data class CreateTimeRecordRequest(
    val title: String,
    val localDateTime: LocalDateTime = LocalDateTime.now(),
    val localDate: LocalDate = LocalDate.now(),
    val localTime: LocalTime = LocalTime.now(),
    val instant: Instant = Instant.now(),
    val zonedDateTime: ZonedDateTime = ZonedDateTime.now(),
    val offsetDateTime: OffsetDateTime = OffsetDateTime.now(),
    val offsetTime: OffsetTime = OffsetTime.now()
) {
    fun toEntity(): TimeRecord = TimeRecord(
        title = title,
        localDateTime = localDateTime,
        localDate = localDate,
        localTime = localTime,
        instant = instant,
        zonedDateTime = zonedDateTime,
        offsetDateTime = offsetDateTime,
        offsetTime = offsetTime
    )

    companion object {
        fun ofCurrentTime(title: String, zoneId: ZoneId): CreateTimeRecordRequest {
            val nowInstant = Instant.now()
            val zoned = nowInstant.atZone(zoneId)
            return CreateTimeRecordRequest(
                title = title,
                localDateTime = zoned.toLocalDateTime(),
                localDate = zoned.toLocalDate(),
                localTime = zoned.toLocalTime(),
                instant = nowInstant,
                zonedDateTime = zoned,
                offsetDateTime = zoned.toOffsetDateTime(),
                offsetTime = OffsetTime.of(zoned.toLocalTime(), zoned.offset)
            )
        }
    }
}

data class TimeRecordResponse(
    val id: String,
    val title: String,
    val localDateTime: LocalDateTime,
    val localDate: LocalDate,
    val localTime: LocalTime,
    val instant: Instant,
    val zonedDateTime: ZonedDateTime,
    val offsetDateTime: OffsetDateTime,
    val offsetTime: OffsetTime,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    companion object {
        fun from(entity: TimeRecord): TimeRecordResponse = TimeRecordResponse(
            id = entity.id,
            title = entity.title,
            localDateTime = entity.localDateTime,
            localDate = entity.localDate,
            localTime = entity.localTime,
            instant = entity.instant,
            zonedDateTime = entity.zonedDateTime,
            offsetDateTime = entity.offsetDateTime,
            offsetTime = entity.offsetTime,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}

data class TimeRecordRawView(
    val entity: TimeRecordResponse,
    val dbEngine: String,
    val columnTypes: Map<String, String>,
    val rawValues: Map<String, String?>
)
