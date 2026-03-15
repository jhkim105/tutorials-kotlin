package com.example.scheduler.application.port

import java.time.Instant

interface ExecutionUseCase {
    fun execute(scheduleId: Long, fireTime: Instant = Instant.now())
}
