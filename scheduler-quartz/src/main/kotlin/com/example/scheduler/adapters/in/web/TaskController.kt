package com.example.scheduler.adapters.`in`.web

import com.example.scheduler.core.application.port.`in`.TaskQueryUseCase
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/tasks")
class TaskController(
    private val taskQueryUseCase: TaskQueryUseCase
) {
    @GetMapping
    fun list(): List<TaskResponse> {
        return taskQueryUseCase.list().map { TaskResponse.from(it) }
    }
}
