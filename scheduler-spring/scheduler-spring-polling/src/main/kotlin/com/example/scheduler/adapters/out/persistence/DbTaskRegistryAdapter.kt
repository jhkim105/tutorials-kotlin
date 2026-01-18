package com.example.scheduler.adapters.out.persistence

import com.example.scheduler.adapters.out.persistence.TaskJpaRepository
import com.example.scheduler.core.application.port.out.TaskHandler
import com.example.scheduler.core.application.port.out.TaskRegistryPort
import com.example.scheduler.core.domain.model.TaskDefinition
import org.springframework.stereotype.Component

@Component
class DbTaskRegistryAdapter(
    taskJpaRepository: TaskJpaRepository,
    handlers: List<TaskHandler>
) : TaskRegistryPort {

    private val handlerMap: Map<String, TaskHandler> = handlers.associateBy { it.taskId() }
    private val repository: TaskJpaRepository = taskJpaRepository

    override fun get(taskId: String): TaskHandler? {
        val exists = repository.findByTaskId(taskId) ?: return null
        return handlerMap[exists.taskId]
    }

    override fun list(): List<TaskDefinition> {
        return repository.findAllByOrderByTaskIdAsc().map {
            TaskDefinition(
                taskId = it.taskId,
                name = it.name,
                description = it.description
            )
        }
    }
}
