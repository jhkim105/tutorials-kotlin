package jhkim105.ssecompare.webfluxflow

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.Valid
import jhkim105.ssecompare.common.BlockingChatMessageStore
import jhkim105.ssecompare.common.EventFactory
import jhkim105.ssecompare.common.MessageRole
import jhkim105.ssecompare.common.ReactiveChatMessageStore
import jhkim105.ssecompare.common.SseMetrics
import jhkim105.ssecompare.common.SseProperties
import jhkim105.ssecompare.common.StreamErrorCodes
import jhkim105.ssecompare.common.StreamEventNames
import jhkim105.ssecompare.common.StreamRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

private val logger = KotlinLogging.logger {}

@RestController
class ChatStreamController(
    private val props: SseProperties,
    private val sseMetrics: SseMetrics,
    private val blockingStore: BlockingChatMessageStore? = null,
    private val reactiveStore: ReactiveChatMessageStore? = null,
) {
    @PostMapping("/chat/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun postStream(@Valid @RequestBody request: StreamRequest): Flow<ServerSentEvent<Any>> = streamFlow(request)

    @GetMapping("/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun getStream(@Valid @ModelAttribute request: StreamRequest): Flow<ServerSentEvent<Any>> = streamFlow(request)

    private fun streamFlow(request: StreamRequest): Flow<ServerSentEvent<Any>> = flow {
        val handle = sseMetrics.openConnection()
        val streamId = UUID.randomUUID().toString()
        val shouldPersist = request.persist && (reactiveStore != null || blockingStore != null)
        val responseContent = if (shouldPersist) StringBuilder() else null

        try {
            if (shouldPersist) {
                try {
                    withTimeout(props.dbSaveTimeoutMs) {
                        saveMessage(streamId, MessageRole.USER, request.prompt)
                    }
                } catch (_: TimeoutCancellationException) {
                    emit(errorEvent(StreamErrorCodes.DB_TIMEOUT, "DB save timed out after ${props.dbSaveTimeoutMs}ms"))
                    return@flow
                } catch (e: Exception) {
                    val code = if (e.message?.contains("Connection is not available", ignoreCase = true) == true) {
                        StreamErrorCodes.DB_TIMEOUT
                    } else {
                        StreamErrorCodes.DB_ERROR
                    }
                    emit(errorEvent(code, e.message ?: "DB save failed"))
                    return@flow
                }
            }

            for (seq in 1..request.events) {
                if (request.failAt == seq) {
                    emit(errorEvent(StreamErrorCodes.SIMULATED_ERROR, "Simulated error at seq $seq"))
                    return@flow
                }

                val msg = EventFactory.createMessage(seq, props.payloadBytes)
                emit(
                    ServerSentEvent.builder<Any>(msg)
                        .event(StreamEventNames.MESSAGE)
                        .build(),
                )
                sseMetrics.recordEventSent()
                responseContent?.append(msg.content)

                if (seq < request.events && request.intervalMs > 0) {
                    var remaining = request.intervalMs
                    val hb = props.heartbeatIntervalMs.coerceAtLeast(10L)
                    while (remaining > 0) {
                        val step = minOf(remaining, hb)
                        delay(step)
                        remaining -= step
                        if (remaining > 0) {
                            emit(ServerSentEvent.builder<Any>().comment(StreamEventNames.HEARTBEAT_COMMENT).build())
                        }
                    }
                }
            }

            val done = EventFactory.createDone(request.events)
            emit(
                ServerSentEvent.builder<Any>(done)
                    .event(StreamEventNames.DONE)
                    .build(),
            )

            if (shouldPersist && responseContent != null) {
                try {
                    saveMessage(streamId, MessageRole.ASSISTANT, responseContent.toString())
                } catch (e: Exception) {
                    logger.warn(e) { "Failed to persist ASSISTANT message for stream=$streamId" }
                    sseMetrics.recordPersistFailure()
                }
            }
        } catch (e: CancellationException) {
            logger.debug { "WebFlux Flow cancelled (stream=$streamId)" }
            throw e
        } catch (e: Exception) {
            logger.warn(e) { "Unexpected error in WebFlux Flow stream=$streamId" }
            emit(errorEvent(StreamErrorCodes.INTERNAL_ERROR, e.message ?: "Internal error"))
        } finally {
            handle.close()
        }
    }

    private suspend fun saveMessage(streamId: String, role: MessageRole, content: String) {
        when {
            reactiveStore != null -> reactiveStore.save(streamId, role, content)
            blockingStore != null -> withContext(Dispatchers.IO) {
                blockingStore.save(streamId, role, content)
            }
        }
    }

    private fun errorEvent(code: String, message: String): ServerSentEvent<Any> =
        ServerSentEvent.builder<Any>(EventFactory.createError(code, message))
            .event(StreamEventNames.ERROR)
            .build()
}
