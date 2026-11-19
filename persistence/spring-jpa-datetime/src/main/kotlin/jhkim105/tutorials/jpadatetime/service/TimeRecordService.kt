package jhkim105.tutorials.jpadatetime.service

import io.github.oshai.kotlinlogging.KotlinLogging
import jhkim105.tutorials.jpadatetime.domain.TimeRecordRepository
import jhkim105.tutorials.jpadatetime.dto.CreateTimeRecordRequest
import jhkim105.tutorials.jpadatetime.dto.TimeRecordRawView
import jhkim105.tutorials.jpadatetime.dto.TimeRecordResponse
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

private val logger = KotlinLogging.logger {}

@Service
class TimeRecordService(
    private val timeRecordRepository: TimeRecordRepository,
    private val dbMetadataService: DbMetadataService
) {

    @Transactional
    fun create(request: CreateTimeRecordRequest): TimeRecordResponse {
        logger.info { "Creating TimeRecord: title=${request.title}" }
        val entity = request.toEntity()
        val saved = timeRecordRepository.save(entity)
        return TimeRecordResponse.from(saved)
    }

    @Transactional(readOnly = true)
    fun findById(id: String): TimeRecordResponse? {
        return timeRecordRepository.findByIdOrNull(id)?.let { TimeRecordResponse.from(it) }
    }

    @Transactional(readOnly = true)
    fun findAll(): List<TimeRecordResponse> {
        return timeRecordRepository.findAll().map { TimeRecordResponse.from(it) }
    }

    @Transactional(readOnly = true)
    fun findBetween(from: OffsetDateTime, to: OffsetDateTime): List<TimeRecordResponse> {
        logger.info { "Searching TimeRecords between $from and $to" }
        return timeRecordRepository.findByOffsetDateTimeBetween(from, to).map { TimeRecordResponse.from(it) }
    }

    @Transactional(readOnly = true)
    fun getRawView(id: String): TimeRecordRawView? {
        val entity = findById(id) ?: return null
        val engine = dbMetadataService.getDatabaseEngine()
        val columnTypes = dbMetadataService.getColumnTypes("time_record")
        val rawValues = dbMetadataService.getRawRowValues(id, "time_record")
        return TimeRecordRawView(
            entity = entity,
            dbEngine = engine,
            columnTypes = columnTypes,
            rawValues = rawValues
        )
    }
}
