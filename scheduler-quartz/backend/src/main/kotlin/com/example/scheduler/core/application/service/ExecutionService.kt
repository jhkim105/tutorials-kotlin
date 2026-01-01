package com.example.scheduler.core.application.service

import com.example.scheduler.core.application.port.`in`.ExecutionUseCase
import com.example.scheduler.core.application.port.out.ExecutionCreateRequest
import com.example.scheduler.core.application.port.out.ExecutionRepositoryPort
import com.example.scheduler.core.application.port.out.TaskRegistryPort
import com.example.scheduler.core.domain.model.Execution
import com.example.scheduler.core.domain.model.ExecutionType
import java.time.Clock

class ExecutionService(
    private val executionRepository: ExecutionRepositoryPort,
    private val taskRegistry: TaskRegistryPort,
    private val clock: Clock
) : ExecutionUseCase {
    override fun manualExecute(taskId: String, payload: String?): Execution {
        val handler = taskRegistry.get(taskId) ?: throw ValidationException("Unknown taskId: $taskId")
        val running = executionRepository.findRunningByTaskId(taskId)
        if (running != null) {
            throw ConflictException("Task already running: $taskId")
        }
        val now = clock.instant()
        val request = ExecutionCreateRequest(
            scheduleId = null,
            taskId = handler.taskId(),
            executionType = ExecutionType.MANUAL,
            payload = payload
        )
        return executionRepository.create(request, now)
    }
}
