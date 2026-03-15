package com.example.scheduler.core.application.port.`in`

import com.example.scheduler.core.domain.model.Execution

interface ExecutionQueryUseCase {
    fun listPage(cursor: String?, limit: Int): ExecutionPage
}
