package com.example.scheduler.core.application.port.`in`

import com.example.scheduler.core.domain.model.Execution

interface ExecutionUseCase {
    fun manualExecute(taskId: String, payload: String?): Execution
}
