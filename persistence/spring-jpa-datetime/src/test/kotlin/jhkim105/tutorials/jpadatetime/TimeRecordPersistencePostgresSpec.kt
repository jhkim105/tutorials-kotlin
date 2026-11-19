package jhkim105.tutorials.jpadatetime

import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import jhkim105.tutorials.jpadatetime.domain.TimeRecord
import jhkim105.tutorials.jpadatetime.domain.TimeRecordRepository
import jhkim105.tutorials.jpadatetime.service.DbMetadataService
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

private val logger = KotlinLogging.logger {}

@SpringBootTest
@ActiveProfiles("postgres")
class TimeRecordPersistencePostgresSpec(
    private val timeRecordRepository: TimeRecordRepository,
    private val dbMetadataService: DbMetadataService
) : BehaviorSpec() {

    override fun extensions() = listOf(SpringExtension)

    init {
        Given("PostgreSQL 16 환경에서 7종 시간 타입 엔티티 저장 및 비교 검증") {
            timeRecordRepository.deleteAll()

            val seoulZone = ZoneId.of("Asia/Seoul")
            val baseDateTime = LocalDateTime.of(2026, 10, 9, 14, 30, 0, 123456000)
            val baseDate = LocalDate.of(2026, 10, 9)
            val baseTime = LocalTime.of(14, 30, 0, 123456000)
            val baseInstant = Instant.parse("2026-10-09T05:30:00.123456Z")
            val baseZonedDateTime = ZonedDateTime.of(baseDateTime, seoulZone)
            val baseOffsetDateTime = OffsetDateTime.of(baseDateTime, ZoneOffset.ofHours(9))
            val baseOffsetTime = OffsetTime.of(baseTime, ZoneOffset.ofHours(9))

            val record = TimeRecord(
                title = "PostgreSQL DateTime Test",
                localDateTime = baseDateTime,
                localDate = baseDate,
                localTime = baseTime,
                instant = baseInstant,
                zonedDateTime = baseZonedDateTime,
                offsetDateTime = baseOffsetDateTime,
                offsetTime = baseOffsetTime
            )

            When("엔티티를 DB에 저장하고 ID로 다시 조회할 때") {
                val saved = timeRecordRepository.save(record)
                val found = timeRecordRepository.findById(saved.id).orElse(null)

                Then("엔티티의 값들이 정상적으로 복원되어야 한다") {
                    found.shouldNotBeNull()
                    found.title shouldBe "PostgreSQL DateTime Test"
                    found.localDate shouldBe baseDate
                    found.localDateTime shouldBe baseDateTime
                    found.localTime shouldBe baseTime
                    found.instant shouldBe baseInstant
                }

                Then("DB 메타데이터 및 raw 컬럼 타입과 실제 저장 문자열을 검증하고 로그로 출력한다") {
                    val columnTypes = dbMetadataService.getColumnTypes("time_record")
                    val rawValues = dbMetadataService.getRawRowValues(saved.id, "time_record")
                    val dbEngine = dbMetadataService.getDatabaseEngine()

                    logger.info { "================================================================================" }
                    logger.info { " [PostgreSQL PERSISTENCE REPORT] Engine: $dbEngine" }
                    logger.info { "--------------------------------------------------------------------------------" }
                    logger.info { String.format("%-18s | %-20s | %-32s", "Entity Field", "DB Column Type", "Raw Stored Value") }
                    logger.info { "--------------------------------------------------------------------------------" }
                    listOf(
                        "local_date_time", "local_date", "local_time",
                        "instant", "zoned_date_time", "offset_date_time", "offset_time",
                        "created_at", "updated_at"
                    ).forEach { col ->
                        logger.info { String.format("%-18s | %-20s | %-32s", col, columnTypes[col] ?: "N/A", rawValues[col] ?: "NULL") }
                    }
                    logger.info { "================================================================================" }

                    columnTypes["offset_date_time"].shouldNotBeNull()
                    rawValues["offset_date_time"].shouldNotBeNull()
                }
            }

            When("OffsetDateTime 범위로 레코드를 검색할 때") {
                val from = baseOffsetDateTime.minusHours(1)
                val to = baseOffsetDateTime.plusHours(1)
                val results = timeRecordRepository.findByOffsetDateTimeBetween(from, to)

                Then("해당 범위 내의 레코드가 정확히 검색되어야 한다") {
                    results shouldHaveSize 1
                }
            }
        }
    }
}
