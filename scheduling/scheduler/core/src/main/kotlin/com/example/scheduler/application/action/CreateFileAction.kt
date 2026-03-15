package com.example.scheduler.application.action

import com.example.scheduler.application.port.ActionHandler
import com.example.scheduler.application.port.FileWriterPort
import com.example.scheduler.domain.action.ActionKey
import com.example.scheduler.domain.model.DeliverySchedule
import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class CreateFileAction(
    private val fileWriter: FileWriterPort,
    private val objectMapper: ObjectMapper
) : ActionHandler {
    private val log = KotlinLogging.logger {}

    override val key: ActionKey = ActionKey.CREATE_FILE

    override fun execute(payload: String, schedule: DeliverySchedule) {
        val parsed = parsePayload(payload)
        val fileName = "${'$'}{parsed.prefix}-${'$'}{Instant.now().toEpochMilli()}.txt"
        val content = parsed.content ?: "created by schedule ${'$'}{schedule.id}"

        fileWriter.writeFile(parsed.directory, fileName, content)
        log.info("[CREATE_FILE] scheduleId={}, directory={}, fileName={}", schedule.id, parsed.directory, fileName)
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
