package com.example.scheduler.infra.action

import com.example.scheduler.application.port.ActionHandler
import com.example.scheduler.domain.action.ActionKey
import com.example.scheduler.domain.model.DeliverySchedule
import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.stereotype.Component

@Component
class HttpPingAction(
    restTemplateBuilder: RestTemplateBuilder,
    private val objectMapper: ObjectMapper
) : ActionHandler {
    private val log = KotlinLogging.logger {}
    private val restTemplate = restTemplateBuilder.build()

    override val key: ActionKey = ActionKey.HTTP_PING

    override fun execute(payload: String, schedule: DeliverySchedule) {
        val parsed = parsePayload(payload)
        val response = restTemplate.getForEntity(parsed.url, String::class.java)
        log.info("[HTTP_PING] scheduleId={}, status={}, url={}", schedule.id, response.statusCode, parsed.url)
    }

    private fun parsePayload(payload: String): HttpPingPayload {
        if (payload.isBlank()) {
            throw IllegalArgumentException("HTTP_PING payload must include url")
        }

        return objectMapper.readValue(payload, HttpPingPayload::class.java)
    }
}

data class HttpPingPayload(
    val url: String
)
