package com.example.scheduler.adapters.`in`.web

import com.example.scheduler.core.application.port.`in`.ExecutionQueryUseCase
import com.example.scheduler.core.application.port.`in`.ExecutionUseCase
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/executions")
class ExecutionController(
    private val executionUseCase: ExecutionUseCase,
    private val executionQueryUseCase: ExecutionQueryUseCase
) {
    @PostMapping("/manual")
    @ResponseStatus(HttpStatus.CREATED)
    fun manualExecute(@RequestBody request: ManualExecutionRequest): ExecutionResponse {
        val execution = executionUseCase.manualExecute(request.actionKey, request.payload)
        return ExecutionResponse.from(execution)
    }

    @GetMapping
    fun list(
        @RequestParam(required = false) cursor: String?,
        @RequestParam(defaultValue = "50") limit: Int
    ): ExecutionPageResponse {
        val page = executionQueryUseCase.listPage(cursor, limit)
        return ExecutionPageResponse(
            items = page.items.map { ExecutionResponse.from(it) },
            nextCursor = page.nextCursor
        )
    }
}
