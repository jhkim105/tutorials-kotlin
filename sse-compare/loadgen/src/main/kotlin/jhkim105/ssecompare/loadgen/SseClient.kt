package jhkim105.ssecompare.loadgen

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.endpoint
import io.ktor.client.request.header
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.utils.io.readUTF8Line
import jhkim105.ssecompare.common.StreamEventNames
import jhkim105.ssecompare.common.StreamRequest
import kotlinx.coroutines.delay

data class StreamOutcome(
    val success: Boolean,
    val ttfbMs: Long? = null,
    val eventGapsMs: List<Long> = emptyList(),
    val eventsReceived: Int = 0,
    val droppedByClient: Boolean = false,
    val error: String? = null,
)

class SseClient(maxConnections: Int = 20_000) : AutoCloseable {
    private val objectMapper = jacksonObjectMapper()

    private val client = HttpClient(CIO) {
        engine {
            this.maxConnectionsCount = maxConnections
            endpoint {
                maxConnectionsPerRoute = maxConnections
                connectTimeout = 15_000
                requestTimeout = 0
                connectAttempts = 1
            }
        }
    }

    suspend fun streamPost(
        url: String,
        request: StreamRequest,
        dropAfterEvents: Int? = null,
        readDelayMs: Long = 0,
    ): StreamOutcome {
        val startNs = System.nanoTime()
        val bodyJson = objectMapper.writeValueAsString(request)

        return try {
            client.preparePost(url) {
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Accept, ContentType.Text.EventStream.toString())
                setBody(bodyJson)
            }.execute { response ->
                if (!response.status.isSuccess()) {
                    return@execute StreamOutcome(
                        success = false,
                        error = "HTTP ${response.status.value}",
                    )
                }

                val channel = response.bodyAsChannel()
                var ttfbMs: Long? = null
                var prevEventNs: Long? = null
                val gaps = mutableListOf<Long>()
                var currentEvent: String? = null
                var receivedMessages = 0
                var sawDone = false
                var sawError = false

                while (!channel.isClosedForRead) {
                    val line = channel.readUTF8Line() ?: break
                    if (readDelayMs > 0 && line.startsWith("data:")) {
                        delay(readDelayMs)
                    }
                    when {
                        line.startsWith("event:") -> {
                            currentEvent = line.removePrefix("event:").trim()
                        }
                        line.startsWith("data:") -> {
                            val nowNs = System.nanoTime()
                            if (ttfbMs == null) {
                                ttfbMs = (nowNs - startNs) / 1_000_000L
                            }
                            when (currentEvent) {
                                StreamEventNames.MESSAGE -> {
                                    prevEventNs?.let { prev ->
                                        gaps.add((nowNs - prev) / 1_000_000L)
                                    }
                                    prevEventNs = nowNs
                                    receivedMessages++
                                    if (dropAfterEvents != null && receivedMessages >= dropAfterEvents) {
                                        return@execute StreamOutcome(
                                            success = true,
                                            ttfbMs = ttfbMs,
                                            eventGapsMs = gaps,
                                            eventsReceived = receivedMessages,
                                            droppedByClient = true,
                                        )
                                    }
                                }
                                StreamEventNames.DONE -> sawDone = true
                                StreamEventNames.ERROR -> sawError = true
                            }
                        }
                        line.isEmpty() -> {
                            currentEvent = null
                        }
                    }
                }

                val ok = sawDone && !sawError && receivedMessages == request.events
                StreamOutcome(
                    success = ok,
                    ttfbMs = ttfbMs,
                    eventGapsMs = gaps,
                    eventsReceived = receivedMessages,
                    error = if (ok) null else "incomplete(received=$receivedMessages,done=$sawDone,err=$sawError)",
                )
            }
        } catch (e: Exception) {
            StreamOutcome(
                success = false,
                error = "${e::class.simpleName}: ${e.message}",
            )
        }
    }

    override fun close() {
        client.close()
    }
}
