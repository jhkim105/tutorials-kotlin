package com.example.scheduler.api

import com.example.scheduler.api.dto.ScheduleRequest
import com.example.scheduler.api.dto.toCommand
import com.example.scheduler.api.dto.toResponse
import com.example.scheduler.application.port.ExecutionUseCase
import com.example.scheduler.application.port.ScheduleUseCase
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/schedules")
class ScheduleController(
    private val scheduleUseCase: ScheduleUseCase,
    private val executionUseCase: ExecutionUseCase
) {
    @PostMapping
    fun create(@Valid @RequestBody request: ScheduleRequest): ResponseEntity<Any> {
        val schedule = scheduleUseCase.create(request.toCommand())
        return ResponseEntity.status(HttpStatus.CREATED).body(schedule.toResponse())
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: ScheduleRequest
    ): ResponseEntity<Any> {
        val schedule = scheduleUseCase.update(id, request.toCommand())
        return ResponseEntity.ok(schedule.toResponse())
    }

    @PostMapping("/{id}/enable")
    fun enable(@PathVariable id: Long): ResponseEntity<Any> {
        val schedule = scheduleUseCase.enable(id)
        return ResponseEntity.ok(schedule.toResponse())
    }

    @PostMapping("/{id}/disable")
    fun disable(@PathVariable id: Long): ResponseEntity<Any> {
        val schedule = scheduleUseCase.disable(id)
        return ResponseEntity.ok(schedule.toResponse())
    }

    @GetMapping
    fun list(): ResponseEntity<Any> {
        val schedules = scheduleUseCase.list().map { it.toResponse() }
        return ResponseEntity.ok(schedules)
    }

    @PostMapping("/{id}/trigger")
    fun trigger(@PathVariable id: Long): ResponseEntity<Any> {
        executionUseCase.execute(id, Instant.now())
        return ResponseEntity.accepted().build()
    }
}
