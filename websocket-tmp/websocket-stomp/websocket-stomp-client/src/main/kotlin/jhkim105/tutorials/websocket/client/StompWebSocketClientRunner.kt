package jhkim105.tutorials.websocket.client

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.messaging.converter.StringMessageConverter
import org.springframework.messaging.simp.stomp.*
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.messaging.WebSocketStompClient
import java.lang.reflect.Type
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Component
class StompWebSocketClientRunner(
    @Value("\${websocket.server-url}") private val serverUrl: String,
    @Value("\${websocket.topic}") private val topic: String,
    @Value("\${websocket.token-url}") private val tokenUrl: String,
    @Value("\${websocket.username}") private val username: String
) {

    private val log = LoggerFactory.getLogger(javaClass)
    private val scheduler = Executors.newSingleThreadScheduledExecutor()
    private val restTemplate = RestTemplate()

    @PostConstruct
    fun connect() {
        connectInternal()
    }

    fun connectInternal() {
        val stompClient = WebSocketStompClient(StandardWebSocketClient())

        // 1. JWT 토큰 발급 요청
        val token = try {
            requestToken(username)
        } catch (e: Exception) {
            log.error("❌ Failed to get token: ${e.message}")
            reconnect()
            return
        }

        // 2. STOMP 헤더에 Authorization 추가
        val connectHeaders = StompHeaders().apply {
            add("Authorization", "Bearer $token")
        }


        stompClient.messageConverter = StringMessageConverter()
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
        }

        // 3. 비동기 연결 시도
        stompClient.connectAsync(serverUrl, sessionHandler).exceptionally {
            log.error("❌ Connection failed: ${it.message}")
            null
        }
    }

    private fun reconnect() {
        scheduler.schedule({ connectInternal() }, 3, TimeUnit.SECONDS)
    }

    private fun requestToken(username: String): String {
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }
        val body = mapOf("username" to username)
        val response = restTemplate.postForObject(
            tokenUrl,
            HttpEntity(body, headers),
            Map::class.java
        )
        return response?.get("token") as? String
            ?: throw IllegalArgumentException("Token not found in response")
    }
}
