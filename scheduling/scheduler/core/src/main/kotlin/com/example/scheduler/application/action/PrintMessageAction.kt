package com.example.scheduler.application.action

import com.example.scheduler.application.port.ActionHandler
import com.example.scheduler.domain.action.ActionKey
import com.example.scheduler.domain.model.DeliverySchedule
import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.springframework.stereotype.Component

@Component
class PrintMessageAction(
    private val objectMapper: ObjectMapper
) : ActionHandler {
    private val log = KotlinLogging.logger {}

    override val key: ActionKey = ActionKey.PRINT_MESSAGE

    override fun execute(payload: String, schedule: DeliverySchedule) {
        val parsed = parsePayload(payload)
        log.info("[PRINT_MESSAGE] scheduleId={}, message={}", schedule.id, parsed.message)
    }

    private fun parsePayload(payload: String): PrintMessagePayload {
        return if (payload.isBlank()) {
            PrintMessagePayload()
        } else {
            objectMapper.readValue(payload, PrintMessagePayload::class.java)
        }
    }
}

data class PrintMessagePayload(
    val message: String = "Hello Scheduler"
)
