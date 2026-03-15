package jhkim105.tutorials.websocket

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebSocketServerTest {

    @LocalServerPort
    var port: Int = 0

    private val restTemplate = RestTemplate()
    private var webSocketSession: org.springframework.web.socket.WebSocketSession? = null

    @AfterEach
    fun tearDown() {
        webSocketSession?.close()
    }

    @Test
    fun `websocket client receives message sent by api`() {
        val messageQueue: BlockingQueue<String> = ArrayBlockingQueue(1)

        val client = StandardWebSocketClient()
        val handler = object : TextWebSocketHandler() {
            override fun handleTextMessage(session: org.springframework.web.socket.WebSocketSession, message: TextMessage) {
                messageQueue.offer(message.payload)
            }
        }

        val wsUrl = "ws://localhost:$port/ws/message"
        val connectFuture: CompletableFuture<WebSocketSession> = client.execute(handler, wsUrl)
        val session = connectFuture.get(5, TimeUnit.SECONDS) // 연결 완료
        webSocketSession = session

        // API 호출하여 메시지 전송
        val apiUrl = "http://localhost:$port/api/send"
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }
        val payload = """{"message": "Hello Test"}"""
        restTemplate.postForEntity(apiUrl, HttpEntity(payload, headers), String::class.java)

        // 메시지 수신 확인
        val received = messageQueue.poll(5, TimeUnit.SECONDS)
        assertEquals("Hello Test", received)
    }
}
