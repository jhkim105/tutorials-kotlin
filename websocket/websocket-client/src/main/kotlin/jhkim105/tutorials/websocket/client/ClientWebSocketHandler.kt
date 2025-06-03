package jhkim105.tutorials.websocket.client

import org.slf4j.LoggerFactory
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

open class ClientWebSocketHandler : TextWebSocketHandler() {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun afterConnectionEstablished(session: WebSocketSession) {
        log.info("✅ Connected to server: ${session.uri}")
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        log.info("📩 Received message from server: ${message.payload}")
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        log.error("❌ Transport error", exception)
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: org.springframework.web.socket.CloseStatus) {
        log.warn("🔌 Connection closed: ${status.reason}")
    }
}
