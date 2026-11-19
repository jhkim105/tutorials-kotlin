package jhkim105.ssecompare.common

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "sse.compare")
data class SseProperties(
    val heartbeatIntervalMs: Long = 15_000L,
    val payloadBytes: Int = 64,
    val emitterTimeoutMs: Long = 300_000L,
    val dbSaveTimeoutMs: Long = 5_000L,
)
