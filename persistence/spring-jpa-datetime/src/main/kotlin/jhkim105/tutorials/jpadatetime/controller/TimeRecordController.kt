package jhkim105.tutorials.jpadatetime.controller

import io.github.oshai.kotlinlogging.KotlinLogging
import jhkim105.tutorials.jpadatetime.dto.CreateTimeRecordRequest
import jhkim105.tutorials.jpadatetime.dto.TimeRecordRawView
import jhkim105.tutorials.jpadatetime.dto.TimeRecordResponse
import jhkim105.tutorials.jpadatetime.service.TimeRecordService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.OffsetDateTime
import java.time.ZoneId

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/api/time-records")
class TimeRecordController(
    private val timeRecordService: TimeRecordService
) {

    @GetMapping
    fun getAll(): List<TimeRecordResponse> {
        return timeRecordService.findAll()
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody request: CreateTimeRecordRequest): TimeRecordResponse {
        return timeRecordService.create(request)
    }

    @PostMapping("/now")
    @ResponseStatus(HttpStatus.CREATED)
    fun createWithCurrentTime(
        @RequestParam(defaultValue = "Asia/Seoul") zone: String,
        @RequestParam(required = false) title: String?
    ): TimeRecordResponse {
        val zoneId = ZoneId.of(zone)
        val defaultTitle = "${zoneId.id} (현재 시각)"
        val request = CreateTimeRecordRequest.ofCurrentTime(title ?: defaultTitle, zoneId)
        return timeRecordService.create(request)
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: String): ResponseEntity<TimeRecordResponse> {
        val record = timeRecordService.findById(id)
        return if (record != null) {
            ResponseEntity.ok(record)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/{id}/db-raw")
    fun getRawView(@PathVariable id: String): ResponseEntity<TimeRecordRawView> {
        val rawView = timeRecordService.getRawView(id)
        return if (rawView != null) {
            ResponseEntity.ok(rawView)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/search")
    fun search(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) from: OffsetDateTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) to: OffsetDateTime
    ): List<TimeRecordResponse> {
        return timeRecordService.findBetween(from, to)
    }
}
