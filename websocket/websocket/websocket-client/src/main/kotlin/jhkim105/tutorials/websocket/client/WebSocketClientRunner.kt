package jhkim105.tutorials.websocket.client

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

//@Component
class WebSocketClientRunner(
    @Value("\${websocket.server-url}") private val serverUrl: String
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @PostConstruct
    fun connectToServer() {
        val client = StandardWebSocketClient()
        val handler = ClientWebSocketHandler()

        log.info("🚀 Connecting to WebSocket server at $serverUrl")

        try {
            val future: CompletableFuture<*> = client.execute(handler, serverUrl)
            future.get(5, TimeUnit.SECONDS)
        } catch (e: Exception) {
            log.error("❌ Failed to connect to WebSocket server", e)
        }
    }
}
