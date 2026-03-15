package com.example.scheduler.core.application.port.`in`

import com.example.scheduler.core.domain.model.Execution

data class ExecutionPage(
    val items: List<Execution>,
    val nextCursor: String?
)
