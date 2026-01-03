package com.example.scheduler.core.application.port.out

import com.example.scheduler.core.domain.model.TaskDefinition

interface TaskHandler {
    fun taskId(): String
    fun definition(): TaskDefinition
    fun execute(payload: String?)
}
