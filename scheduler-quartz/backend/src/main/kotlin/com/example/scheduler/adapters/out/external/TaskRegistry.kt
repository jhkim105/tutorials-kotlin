package com.example.scheduler.adapters.out.external

import com.example.scheduler.core.application.port.out.TaskHandler
import com.example.scheduler.core.application.port.out.TaskRegistryPort
import com.example.scheduler.core.domain.model.TaskDefinition
import org.springframework.stereotype.Component

@Component
class SpringTaskRegistry(handlers: List<TaskHandler>) : TaskRegistryPort {
    private val handlerMap: Map<String, TaskHandler> = handlers.associateBy { it.taskId() }

    override fun get(taskId: String): TaskHandler? = handlerMap[taskId]

    override fun list(): List<TaskDefinition> = handlerMap.values.map { it.definition() }
}
