package jhkim105.ssecompare.mvcsseemitter

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PreDestroy
import jakarta.validation.Valid
import jhkim105.ssecompare.common.BlockingChatMessageStore
import jhkim105.ssecompare.common.EventFactory
import jhkim105.ssecompare.common.MessageRole
import jhkim105.ssecompare.common.SseMetrics
import jhkim105.ssecompare.common.SseProperties
import jhkim105.ssecompare.common.StreamErrorCodes
import jhkim105.ssecompare.common.StreamEventNames
import jhkim105.ssecompare.common.StreamRequest
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException
import java.util.UUID
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicBoolean

private val logger = KotlinLogging.logger {}

@Configuration
class SseExecutorConfig(private val environment: Environment) {
    private var executor: ExecutorService? = null

    @Bean("sseTaskExecutor")
    fun sseTaskExecutor(): ExecutorService {
        val useVt = environment.getProperty("spring.threads.virtual.enabled", Boolean::class.java, false)
        val exec = if (useVt) {
            logger.info { "Initializing VirtualThreadPerTaskExecutor for SseEmitter (Config B)" }
            Executors.newVirtualThreadPerTaskExecutor()
        } else {
            logger.info { "Initializing FixedThreadPool(200) for SseEmitter (Config A)" }
            Executors.newFixedThreadPool(200)
        }
        executor = exec
        return exec
    }

    @PreDestroy
    fun shutdown() {
        executor?.shutdownNow()
    }
}

@RestController
class ChatStreamController(
    @Qualifier("sseTaskExecutor") private val sseExecutor: ExecutorService,
    private val props: SseProperties,
    private val sseMetrics: SseMetrics,
    private val blockingStore: BlockingChatMessageStore? = null,
) {
    @PostMapping("/chat/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun postStream(@Valid @RequestBody request: StreamRequest): SseEmitter = startStream(request)

    @GetMapping("/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun getStream(@Valid @ModelAttribute request: StreamRequest): SseEmitter = startStream(request)

    private fun startStream(request: StreamRequest): SseEmitter {
        val emitter = SseEmitter(props.emitterTimeoutMs)
        val handle = sseMetrics.openConnection()
        val cancelled = AtomicBoolean(false)
        var taskFuture: Future<*>? = null

        val cleanup = {
            cancelled.set(true)
            taskFuture?.cancel(true)
            handle.close()
        }

        emitter.onCompletion(cleanup)
        emitter.onTimeout {
            cleanup()
            emitter.complete()
        }
        emitter.onError {
            cleanup()
        }

        taskFuture = sseExecutor.submit {
            val streamId = UUID.randomUUID().toString()
            val shouldPersist = request.persist && blockingStore != null
            val responseContent = if (shouldPersist) StringBuilder() else null

            try {
                if (shouldPersist) {
                    val saved = saveBlockingWithTimeout(streamId, MessageRole.USER, request.prompt)
                    if (!saved.ok) {
                        sendErrorAndComplete(emitter, saved.errorCode!!, saved.errorMessage!!)
                        return@submit
                    }
                }

                for (seq in 1..request.events) {
                    if (cancelled.get() || Thread.currentThread().isInterrupted) return@submit

                    if (request.failAt == seq) {
                        sendErrorAndComplete(
                            emitter,
                            StreamErrorCodes.SIMULATED_ERROR,
                            "Simulated error at seq $seq",
                        )
                        return@submit
                    }

                    val msg = EventFactory.createMessage(seq, props.payloadBytes)
                    emitter.send(
                        SseEmitter.event()
                            .name(StreamEventNames.MESSAGE)
                            .data(msg, MediaType.APPLICATION_JSON),
                    )
                    sseMetrics.recordEventSent()
                    responseContent?.append(msg.content)

                    if (seq < request.events && request.intervalMs > 0) {
                        sleepWithHeartbeats(emitter, request.intervalMs, cancelled)
                    }
                }

                if (cancelled.get() || Thread.currentThread().isInterrupted) return@submit

                val done = EventFactory.createDone(request.events)
                emitter.send(
                    SseEmitter.event()
                        .name(StreamEventNames.DONE)
                        .data(done, MediaType.APPLICATION_JSON),
                )
                emitter.complete()

                if (shouldPersist && responseContent != null) {
                    try {
                        blockingStore!!.save(streamId, MessageRole.ASSISTANT, responseContent.toString())
                    } catch (e: Exception) {
                        logger.warn(e) { "Failed to persist ASSISTANT message for stream=$streamId" }
                        sseMetrics.recordPersistFailure()
                    }
                }
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
            } catch (_: IOException) {
                logger.debug { "Client disconnected during SSE send (stream=$streamId)" }
            } catch (_: IllegalStateException) {
                logger.debug { "Emitter already completed (stream=$streamId)" }
            } catch (e: Exception) {
                logger.warn(e) { "Unexpected error in SSE stream=$streamId" }
                runCatching {
                    sendErrorAndComplete(emitter, StreamErrorCodes.INTERNAL_ERROR, e.message ?: "Internal error")
                }
            } finally {
                handle.close()
            }
        }

        return emitter
    }

    private fun sleepWithHeartbeats(emitter: SseEmitter, totalMs: Long, cancelled: AtomicBoolean) {
        var remaining = totalMs
        val hb = props.heartbeatIntervalMs.coerceAtLeast(10L)
        while (remaining > 0 && !cancelled.get()) {
            val step = minOf(remaining, hb)
            Thread.sleep(step)
            remaining -= step
            if (remaining > 0 && !cancelled.get()) {
                emitter.send(SseEmitter.event().comment(StreamEventNames.HEARTBEAT_COMMENT))
            }
        }
    }

    private fun sendErrorAndComplete(emitter: SseEmitter, code: String, message: String) {
        val err = EventFactory.createError(code, message)
        emitter.send(
            SseEmitter.event()
                .name(StreamEventNames.ERROR)
                .data(err, MediaType.APPLICATION_JSON),
        )
        emitter.complete()
    }

    private data class SaveOutcome(val ok: Boolean, val errorCode: String? = null, val errorMessage: String? = null)

    private fun saveBlockingWithTimeout(streamId: String, role: MessageRole, content: String): SaveOutcome {
        val fut = sseExecutor.submit {
            blockingStore!!.save(streamId, role, content)
        }
        return try {
            fut.get(props.dbSaveTimeoutMs, TimeUnit.MILLISECONDS)
            SaveOutcome(ok = true)
        } catch (_: TimeoutException) {
            fut.cancel(true)
            SaveOutcome(false, StreamErrorCodes.DB_TIMEOUT, "DB save timed out after ${props.dbSaveTimeoutMs}ms")
        } catch (e: Exception) {
            val cause = e.cause ?: e
            val code = if (cause.message?.contains("Connection is not available", ignoreCase = true) == true) {
                StreamErrorCodes.DB_TIMEOUT
            } else {
                StreamErrorCodes.DB_ERROR
            }
            SaveOutcome(false, code, cause.message ?: "DB save failed")
        }
    }
}
