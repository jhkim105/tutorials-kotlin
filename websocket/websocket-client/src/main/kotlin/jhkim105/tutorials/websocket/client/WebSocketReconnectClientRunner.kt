package jhkim105.tutorials.websocket.client

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Component
class WebSocketReconnectClientRunner(
    @Value("\${websocket.server-url}") private val serverUrl: String
) {

    private val log = LoggerFactory.getLogger(javaClass)
    private val scheduler = Executors.newSingleThreadScheduledExecutor()
    private val client = StandardWebSocketClient()

    private val handler: WebSocketHandler = object : ClientWebSocketHandler() {
        override fun afterConnectionClosed(
            session: WebSocketSession,
            status: org.springframework.web.socket.CloseStatus
        ) {
            log.warn("⚠️ Connection closed. Reason: ${status.reason}")
            reconnectWithDelay()
        }

        override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
            log.error("❌ Transport error", exception)
            reconnectWithDelay()
        }
    }

    @PostConstruct
    fun connect() {
        connectInternal()
    }

    private fun connectInternal() {
        try {
            log.info("🔌 Connecting to $serverUrl...")
            val future = client.execute(handler, serverUrl)
            future.get(5, TimeUnit.SECONDS)
        } catch (e: Exception) {
            log.error("❌ Initial connection failed: ${e.message}")
            reconnectWithDelay()
        }
    }

    private fun reconnectWithDelay() {
        scheduler.schedule({
            log.info("⏳ Reconnecting to WebSocket server...")
            connectInternal()
        }, 3, TimeUnit.SECONDS)
    }
}
