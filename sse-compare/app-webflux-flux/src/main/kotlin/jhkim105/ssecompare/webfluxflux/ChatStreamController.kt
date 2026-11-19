package jhkim105.ssecompare.webfluxflux

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.Valid
import jhkim105.ssecompare.common.BlockingChatMessageStore
import jhkim105.ssecompare.common.EventFactory
import jhkim105.ssecompare.common.MessageRole
import jhkim105.ssecompare.common.SimulatedStreamException
import jhkim105.ssecompare.common.SseMetrics
import jhkim105.ssecompare.common.SseProperties
import jhkim105.ssecompare.common.StreamErrorCodes
import jhkim105.ssecompare.common.StreamEventNames
import jhkim105.ssecompare.common.StreamRequest
import jhkim105.ssecompare.persistence.r2dbc.R2dbcChatMessageStore
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import reactor.core.scheduler.Schedulers
import java.time.Duration
import java.util.UUID
import java.util.concurrent.TimeoutException

private val logger = KotlinLogging.logger {}

@RestController
class ChatStreamController(
    private val props: SseProperties,
    private val sseMetrics: SseMetrics,
    private val blockingStore: BlockingChatMessageStore? = null,
    private val r2dbcStore: R2dbcChatMessageStore? = null,
) {
    @PostMapping("/chat/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun postStream(@Valid @RequestBody request: StreamRequest): Flux<ServerSentEvent<Any>> = buildFlux(request)

    @GetMapping("/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun getStream(@Valid @ModelAttribute request: StreamRequest): Flux<ServerSentEvent<Any>> = buildFlux(request)

    private fun buildFlux(request: StreamRequest): Flux<ServerSentEvent<Any>> = Flux.defer {
        val handle = sseMetrics.openConnection()
        val streamId = UUID.randomUUID().toString()
        val shouldPersist = request.persist && (r2dbcStore != null || blockingStore != null)
        val responseContent = if (shouldPersist) StringBuilder() else null
        val doneSignal = Sinks.empty<Void>()

        val startPersistMono: Mono<Void> = if (shouldPersist) {
            saveAsMono(streamId, MessageRole.USER, request.prompt)
                .timeout(Duration.ofMillis(props.dbSaveTimeoutMs))
        } else {
            Mono.empty()
        }

        val messageFlux: Flux<ServerSentEvent<Any>> = Flux.range(1, request.events)
            .concatMap { seq ->
                val itemMono = Mono.fromCallable {
                    if (request.failAt == seq) {
                        throw SimulatedStreamException(message = "Simulated error at seq $seq")
                    }
                    val msg = EventFactory.createMessage(seq, props.payloadBytes)
                    sseMetrics.recordEventSent()
                    responseContent?.append(msg.content)
                    ServerSentEvent.builder<Any>(msg)
                        .event(StreamEventNames.MESSAGE)
                        .build()
                }
                if (seq > 1 && request.intervalMs > 0) {
                    Mono.delay(Duration.ofMillis(request.intervalMs)).then(itemMono)
                } else {
                    itemMono
                }
            }

        val doneMono: Mono<ServerSentEvent<Any>> = Mono.fromCallable {
            ServerSentEvent.builder<Any>(EventFactory.createDone(request.events))
                .event(StreamEventNames.DONE)
                .build()
        }

        val endPersistMono: Mono<Void> = if (shouldPersist && responseContent != null) {
            saveAsMono(streamId, MessageRole.ASSISTANT, responseContent.toString())
                .onErrorResume { e ->
                    logger.warn(e) { "Failed to persist ASSISTANT message for stream=$streamId" }
                    sseMetrics.recordPersistFailure()
                    Mono.empty()
                }
        } else {
            Mono.empty()
        }

        val mainFlux: Flux<ServerSentEvent<Any>> = startPersistMono
            .thenMany(messageFlux.concatWith(doneMono).concatWith(endPersistMono.thenMany(Flux.empty())))
            .onErrorResume { ex ->
                val (code, message) = when (ex) {
                    is SimulatedStreamException -> ex.code to ex.message
                    is TimeoutException -> StreamErrorCodes.DB_TIMEOUT to "DB save timed out after ${props.dbSaveTimeoutMs}ms"
                    else -> {
                        val c = if (ex.message?.contains("Connection is not available", ignoreCase = true) == true) {
                            StreamErrorCodes.DB_TIMEOUT
                        } else if (shouldPersist) {
                            StreamErrorCodes.DB_ERROR
                        } else {
                            StreamErrorCodes.INTERNAL_ERROR
                        }
                        c to (ex.message ?: "Stream error")
                    }
                }
                Mono.just(
                    ServerSentEvent.builder<Any>(EventFactory.createError(code, message))
                        .event(StreamEventNames.ERROR)
                        .build(),
                )
            }
            .doFinally {
                doneSignal.tryEmitEmpty()
            }

        val hbInterval = props.heartbeatIntervalMs.coerceAtLeast(10L)
        val heartbeatFlux: Flux<ServerSentEvent<Any>> = Flux.interval(Duration.ofMillis(hbInterval))
            .map { ServerSentEvent.builder<Any>().comment(StreamEventNames.HEARTBEAT_COMMENT).build() }
            .takeUntilOther(doneSignal.asMono())

        Flux.merge(mainFlux, heartbeatFlux)
            .doFinally { handle.close() }
    }

    private fun saveAsMono(streamId: String, role: MessageRole, content: String): Mono<Void> {
        return when {
            r2dbcStore != null -> r2dbcStore.saveMono(streamId, role, content).then()
            blockingStore != null -> Mono.fromRunnable<Void> {
                blockingStore.save(streamId, role, content)
            }.subscribeOn(Schedulers.boundedElastic())
            else -> Mono.empty()
        }
    }
}
