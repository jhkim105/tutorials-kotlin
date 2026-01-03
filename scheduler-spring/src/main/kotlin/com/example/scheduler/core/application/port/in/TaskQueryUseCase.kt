package com.example.scheduler.core.application.port.`in`

import com.example.scheduler.core.domain.model.TaskDefinition

interface TaskQueryUseCase {
    fun list(): List<TaskDefinition>
}
