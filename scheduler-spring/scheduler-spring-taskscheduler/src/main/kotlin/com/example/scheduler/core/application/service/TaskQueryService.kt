package com.example.scheduler.core.application.service

import com.example.scheduler.core.application.port.`in`.TaskQueryUseCase
import com.example.scheduler.core.application.port.out.TaskRegistryPort

class TaskQueryService(
    private val taskRegistry: TaskRegistryPort
) : TaskQueryUseCase {
    override fun list() = taskRegistry.list()
}
