package com.example.scheduler.adapters.`in`.web

import com.example.scheduler.core.domain.model.TaskDefinition

data class TaskResponse(
    val taskId: String,
    val name: String,
    val description: String
) {
    companion object {
        fun from(definition: TaskDefinition): TaskResponse {
            return TaskResponse(
                taskId = definition.taskId,
                name = definition.name,
                description = definition.description
            )
        }
    }
}
