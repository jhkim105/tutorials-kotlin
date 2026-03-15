package com.example.scheduler.core.application.port.out

import com.example.scheduler.core.domain.model.TaskDefinition

interface TaskRegistryPort {
    fun get(taskId: String): TaskHandler?
    fun list(): List<TaskDefinition>
}
