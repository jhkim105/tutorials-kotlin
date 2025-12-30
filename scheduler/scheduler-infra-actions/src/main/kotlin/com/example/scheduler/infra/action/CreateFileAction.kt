package com.example.scheduler.infra.action

import com.example.scheduler.application.port.ActionHandler
import com.example.scheduler.domain.action.ActionKey
import com.example.scheduler.domain.model.DeliverySchedule
import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Instant

@Component
class CreateFileAction(
    private val objectMapper: ObjectMapper
) : ActionHandler {
    private val log = KotlinLogging.logger {}

    override val key: ActionKey = ActionKey.CREATE_FILE

    override fun execute(payload: String, schedule: DeliverySchedule) {
        val parsed = parsePayload(payload)
        val directory = Paths.get(parsed.directory)
        Files.createDirectories(directory)

        val fileName = "${parsed.prefix}-${Instant.now().toEpochMilli()}.txt"
        val filePath = directory.resolve(fileName)
        val content = parsed.content ?: "created by schedule ${schedule.id}"

        Files.writeString(filePath, content)
        log.info("[CREATE_FILE] scheduleId={}, path={}", schedule.id, filePath)
    }

    private fun parsePayload(payload: String): CreateFilePayload {
        return if (payload.isBlank()) {
            CreateFilePayload()
        } else {
            objectMapper.readValue(payload, CreateFilePayload::class.java)
        }
    }
}

data class CreateFilePayload(
    val directory: String = "data/out",
    val prefix: String = "sample",
    val content: String? = null
)
