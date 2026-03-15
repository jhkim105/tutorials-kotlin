package com.example.scheduler.core.application.service

import com.example.scheduler.core.application.port.`in`.ExecutionPage
import com.example.scheduler.core.application.port.`in`.ExecutionQueryUseCase
import com.example.scheduler.core.application.port.out.ExecutionRepositoryPort
import com.example.scheduler.core.domain.model.Execution

class ExecutionQueryService(
    private val executionRepository: ExecutionRepositoryPort
) : ExecutionQueryUseCase {
    override fun listPage(cursor: String?, limit: Int): ExecutionPage {
        val size = limit.coerceIn(1, 100)
        val items = if (cursor.isNullOrBlank()) {
            executionRepository.findPage(size)
        } else {
            val decoded = decodeCursor(cursor)
            executionRepository.findPageAfter(decoded.createdAt, decoded.executionId, size)
        }
        val nextCursor = if (items.size == size) {
            items.lastOrNull()?.let { encodeCursor(it) }
        } else {
            null
        }
        return ExecutionPage(items = items, nextCursor = nextCursor)
    }
}

private data class ExecutionCursor(val createdAt: java.time.Instant, val executionId: String)

private fun encodeCursor(execution: Execution): String {
    return "${execution.createdAt.toEpochMilli()}_${execution.executionId}"
}

private fun decodeCursor(cursor: String): ExecutionCursor {
    val parts = cursor.split("_", limit = 2)
    if (parts.size != 2) {
        throw ValidationException("Invalid cursor")
    }
    val epoch = parts[0].toLongOrNull() ?: throw ValidationException("Invalid cursor")
    return ExecutionCursor(java.time.Instant.ofEpochMilli(epoch), parts[1])
}
