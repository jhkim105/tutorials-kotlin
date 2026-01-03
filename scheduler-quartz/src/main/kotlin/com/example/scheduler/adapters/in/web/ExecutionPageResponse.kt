package com.example.scheduler.adapters.`in`.web

data class ExecutionPageResponse(
    val items: List<ExecutionResponse>,
    val nextCursor: String?
)
