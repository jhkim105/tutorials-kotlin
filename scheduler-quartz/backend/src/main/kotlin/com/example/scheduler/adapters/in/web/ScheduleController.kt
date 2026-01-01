package com.example.scheduler.adapters.`in`.web

import com.example.scheduler.core.application.port.`in`.ScheduleCreateCommand
import com.example.scheduler.core.application.port.`in`.ScheduleUpdateCommand
import com.example.scheduler.core.application.port.`in`.ScheduleUseCase
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/schedules")
class ScheduleController(
    private val scheduleUseCase: ScheduleUseCase
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody request: ScheduleRequest): ScheduleResponse {
        val schedule = scheduleUseCase.create(
            ScheduleCreateCommand(
                name = request.name,
                scheduleType = request.scheduleType,
                cronExpression = request.cronExpression,
                runAt = request.runAt,
                enabled = request.enabled,
                taskId = request.actionKey,
                payload = request.payload
            )
        )
        return ScheduleResponse.from(schedule)
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: String, @RequestBody request: ScheduleRequest): ScheduleResponse {
        val schedule = scheduleUseCase.update(
            id,
            ScheduleUpdateCommand(
                name = request.name,
                scheduleType = request.scheduleType,
                cronExpression = request.cronExpression,
                runAt = request.runAt,
                enabled = request.enabled,
                taskId = request.actionKey,
                payload = request.payload
            )
        )
        return ScheduleResponse.from(schedule)
    }

    @PatchMapping("/{id}/enable")
    fun enable(@PathVariable id: String): ScheduleResponse {
        return ScheduleResponse.from(scheduleUseCase.enable(id))
    }

    @PatchMapping("/{id}/disable")
    fun disable(@PathVariable id: String): ScheduleResponse {
        return ScheduleResponse.from(scheduleUseCase.disable(id))
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: String) {
        scheduleUseCase.delete(id)
    }

    @GetMapping
    fun list(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): SchedulePageResponse {
        val limit = size.coerceIn(1, 100)
        val safePage = page.coerceAtLeast(1)
        val offset = (safePage - 1) * limit
        val result = scheduleUseCase.listPage(offset, limit)
        return SchedulePageResponse(
            items = result.items.map { ScheduleResponse.from(it) },
            total = result.total,
            limit = result.limit,
            offset = result.offset
        )
    }

    @GetMapping("/{id}")
    fun get(@PathVariable id: String): ScheduleResponse {
        return ScheduleResponse.from(scheduleUseCase.get(id))
    }
}
