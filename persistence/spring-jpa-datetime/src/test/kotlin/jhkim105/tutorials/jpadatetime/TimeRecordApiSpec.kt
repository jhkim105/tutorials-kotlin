package jhkim105.tutorials.jpadatetime

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import jhkim105.tutorials.jpadatetime.domain.TimeRecordRepository
import jhkim105.tutorials.jpadatetime.dto.CreateTimeRecordRequest
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("postgres")
class TimeRecordApiSpec(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    private val timeRecordRepository: TimeRecordRepository
) : BehaviorSpec() {

    override fun extensions() = listOf(SpringExtension)

    init {
        Given("TimeRecord REST API 테스트 환경") {
            timeRecordRepository.deleteAll()

            val request = CreateTimeRecordRequest(
                title = "API Serialization Test",
                localDateTime = LocalDateTime.of(2026, 10, 9, 8, 30, 0, 123456000),
                localDate = LocalDate.of(2026, 10, 9),
                localTime = LocalTime.of(8, 30, 0, 123456000),
                instant = Instant.parse("2026-10-08T23:30:00.123456Z"),
                zonedDateTime = ZonedDateTime.of(2026, 10, 9, 8, 30, 0, 123456000, ZoneOffset.ofHours(9)),
                offsetDateTime = OffsetDateTime.of(2026, 10, 9, 8, 30, 0, 123456000, ZoneOffset.ofHours(9)),
                offsetTime = OffsetTime.of(8, 30, 0, 123456000, ZoneOffset.ofHours(9))
            )

            When("POST /api/time-records 호출 시") {
                val postResponse = mockMvc.post("/api/time-records") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(request)
                }.andExpect {
                    status { isCreated() }
                    jsonPath("$.id") { exists() }
                    jsonPath("$.title") { value("API Serialization Test") }
                    jsonPath("$.localDate") { value("2026-10-09") }
                    jsonPath("$.localDateTime") { value("2026-10-09T08:30:00.123456") }
                }.andReturn()

                val createdId = objectMapper.readTree(postResponse.response.contentAsString).get("id").asText()

                Then("GET /api/time-records/{id} 호출 시 200 OK와 ISO-8601 포맷으로 반환되어야 한다") {
                    mockMvc.get("/api/time-records/$createdId") {
                        accept = MediaType.APPLICATION_JSON
                    }.andExpect {
                        status { isOk() }
                        jsonPath("$.id") { value(createdId) }
                        jsonPath("$.localDate") { value("2026-10-09") }
                        jsonPath("$.localDateTime") { value("2026-10-09T08:30:00.123456") }
                        jsonPath("$.instant") { value("2026-10-08T23:30:00.123456Z") }
                    }
                }

                Then("GET /api/time-records/{id}/db-raw 호출 시 메타데이터 및 물리 컬럼 정보가 포함되어야 한다") {
                    mockMvc.get("/api/time-records/$createdId/db-raw") {
                        accept = MediaType.APPLICATION_JSON
                    }.andExpect {
                        status { isOk() }
                        jsonPath("$.dbEngine") { exists() }
                        jsonPath("$.columnTypes") { isMap() }
                        jsonPath("$.rawValues") { isMap() }
                        jsonPath("$.entity.id") { value(createdId) }
                    }
                }

                Then("GET /api/time-records/search 로 시간 범위 검색 시 결과가 정상 반환되어야 한다") {
                    mockMvc.get("/api/time-records/search") {
                        param("from", "2026-10-09T07:00:00+09:00")
                        param("to", "2026-10-09T10:00:00+09:00")
                    }.andExpect {
                        status { isOk() }
                        jsonPath("$.length()") { value(1) }
                    }
                }
            }
        }
    }
}
