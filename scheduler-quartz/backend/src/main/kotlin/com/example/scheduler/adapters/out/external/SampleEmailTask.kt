package com.example.scheduler.adapters.out.external

import com.example.scheduler.core.application.port.out.TaskHandler
import com.example.scheduler.core.domain.model.TaskDefinition
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

@Component
class SampleEmailTask : TaskHandler {
    override fun taskId(): String = "sampleEmail"

    override fun definition(): TaskDefinition {
        return TaskDefinition(
            taskId = taskId(),
            name = "Sample Email",
            description = "Example task that simulates sending email."
        )
    }

    override fun execute(payload: String?) {
        log.info { "Running email task with payload=$payload" }
    }
}
