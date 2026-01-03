package com.example.scheduler.adapters.out.task

import com.example.scheduler.core.application.port.out.TaskHandler
import com.example.scheduler.core.domain.model.TaskDefinition
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

@Component
class SampleCleanupTask : TaskHandler {
    override fun taskId(): String = "sampleCleanup"

    override fun definition(): TaskDefinition {
        return TaskDefinition(
            taskId = taskId(),
            name = "Sample Cleanup",
            description = "Example task that simulates cleanup work."
        )
    }

    override fun execute(payload: String?) {
        log.info { "Running cleanup task with payload=$payload" }
    }
}
