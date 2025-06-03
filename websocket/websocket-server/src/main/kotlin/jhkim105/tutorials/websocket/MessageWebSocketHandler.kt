package jhkim105.tutorials.websocket

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.CopyOnWriteArraySet

@Component
class MessageWebSocketHandler : TextWebSocketHandler() {

    private val sessions = CopyOnWriteArraySet<WebSocketSession>()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        sessions.add(session)
        log.info("✅ Client connected: ${session.id}")
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: org.springframework.web.socket.CloseStatus) {
        sessions.remove(session)
        log.info("❌ Client disconnected: ${session.id}")
    }

    fun broadcast(message: String) {
        sessions.forEach {
            if (it.isOpen) {
                it.sendMessage(TextMessage(message))
            }
        }
        println("📤 Sent to ${sessions.size} clients: $message")
    }

    companion object {
        private val log = LoggerFactory.getLogger(MessageWebSocketHandler::class.java)
    }
}
