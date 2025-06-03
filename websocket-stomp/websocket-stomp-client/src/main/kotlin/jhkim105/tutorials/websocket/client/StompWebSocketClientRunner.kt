package jhkim105.tutorials.websocket.client

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.messaging.converter.StringMessageConverter
import org.springframework.messaging.simp.stomp.*
import org.springframework.stereotype.Component
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.messaging.WebSocketStompClient
import java.lang.reflect.Type
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Component
class StompWebSocketClientRunner(
    @Value("\${websocket.server-url}") private val serverUrl: String,
    @Value("\${websocket.topic}") private val topic: String
) {

    private val log = LoggerFactory.getLogger(javaClass)
    private val scheduler = Executors.newSingleThreadScheduledExecutor()

    @PostConstruct
    fun connect() {
        connectInternal()
    }

    fun connectInternal() {
        val stompClient = WebSocketStompClient(StandardWebSocketClient())
        val sessionHandler = object : StompSessionHandlerAdapter() {

            override fun afterConnected(session: StompSession, connectedHeaders: StompHeaders) {
                log.info("✅ Connected to STOMP server at $serverUrl")
                session.subscribe(topic, object : StompFrameHandler {
                    override fun getPayloadType(headers: StompHeaders): Type = String::class.java
                    override fun handleFrame(headers: StompHeaders, payload: Any?) {
                        log.info("📩 Received: $payload")
                    }
                })
            }

            override fun handleTransportError(session: StompSession, exception: Throwable) {
                log.error("❌ Transport error", exception)
                reconnect()
            }

            override fun handleException(
                session: StompSession,
                command: StompCommand?,
                headers: StompHeaders,
                payload: ByteArray,
                exception: Throwable
            ) {
                log.error("❌ Exception", exception)
                reconnect()
            }

            fun reconnect() {
                scheduler.schedule({ connectInternal() }, 3, TimeUnit.SECONDS)
            }
        }

        stompClient.messageConverter = StringMessageConverter()
        stompClient.connectAsync(serverUrl, sessionHandler).exceptionally {
            log.error("❌ Connection failed: ${it.message}")
            null
        }
    }
}
