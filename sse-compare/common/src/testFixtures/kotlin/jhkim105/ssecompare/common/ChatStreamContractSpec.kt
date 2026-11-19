package jhkim105.ssecompare.common

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.server.LocalServerPort
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration

data class ParsedSseItem(
    val event: String? = null,
    val data: String? = null,
    val comment: String? = null,
    val receivedAtMs: Long = System.currentTimeMillis(),
)

abstract class ChatStreamContractSpec : BehaviorSpec() {
    override fun extensions() = listOf(SpringExtension)

    @LocalServerPort
    protected var port: Int = 0

    @Autowired
    protected lateinit var sseMetrics: SseMetrics

    private val objectMapper = jacksonObjectMapper()
    private val httpClient: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build()

    protected fun baseUrl(): String = "http://localhost:$port"

    init {
        given("POST /chat/stream 정상 요청") {
            `when`("events=4, intervalMs=40 요청을 보내면") {
                then("text/event-stream 으로 4개 message 이벤트와 done 이벤트를 순서대로 수신한다") {
                    val req = StreamRequest(prompt = "hello sse", events = 4, intervalMs = 40)
                    val startMs = System.currentTimeMillis()
                    val response = postSse(req)
                    val items = readAllSse(response)
                    val elapsedMs = System.currentTimeMillis() - startMs

                    response.statusCode() shouldBe 200
                    response.headers().firstValue("Content-Type").orElse("") shouldContain "text/event-stream"

                    val messages = items.filter { it.event == StreamEventNames.MESSAGE }
                    val dones = items.filter { it.event == StreamEventNames.DONE }

                    messages shouldHaveSize 4
                    messages.mapIndexed { idx, item ->
                        val payload = objectMapper.readValue<MessageEventData>(item.data!!)
                        payload.seq shouldBe idx + 1
                        payload.content.length shouldBe 64
                    }

                    dones shouldHaveSize 1
                    val donePayload = objectMapper.readValue<DoneEventData>(dones.first().data!!)
                    donePayload.total shouldBe 4

                    // FR-010: 3 intervals of 40ms should take at least ~90ms
                    elapsedMs shouldBeGreaterThanOrEqual 80L

                    waitUntilZeroActiveConnections()
                    sseMetrics.activeConnections() shouldBe 0
                }
            }
        }

        given("GET /stream 기준선 요청") {
            `when`("쿼리 파라미터로 events=3, intervalMs=20 요청을 보내면") {
                then("POST 와 동일한 형식의 SSE 스트림을 수신한다") {
                    val encodedPrompt = URLEncoder.encode("hello get", StandardCharsets.UTF_8)
                    val httpReq = HttpRequest.newBuilder()
                        .uri(URI.create("${baseUrl()}/stream?prompt=$encodedPrompt&events=3&intervalMs=20"))
                        .GET()
                        .build()

                    val response = httpClient.send(httpReq, HttpResponse.BodyHandlers.ofInputStream())
                    val items = readAllSse(response)

                    response.statusCode() shouldBe 200
                    response.headers().firstValue("Content-Type").orElse("") shouldContain "text/event-stream"

                    items.filter { it.event == StreamEventNames.MESSAGE } shouldHaveSize 3
                    items.filter { it.event == StreamEventNames.DONE } shouldHaveSize 1

                    waitUntilZeroActiveConnections()
                    sseMetrics.activeConnections() shouldBe 0
                }
            }
        }

        given("잘못된 요청 검증 (400)") {
            `when`("범위 밖이거나 필수값이 누락된 요청을 보내면") {
                then("스트림 시작 전 400 과 INVALID_REQUEST 응답을 반환한다") {
                    val invalidBodies = listOf(
                        """{"prompt":"","events":3,"intervalMs":10}""",
                        """{"prompt":"hi","events":0,"intervalMs":10}""",
                        """{"prompt":"hi","events":100001,"intervalMs":10}""",
                        """{"prompt":"hi","events":3,"intervalMs":-1}""",
                        """{"prompt":"hi","events":3,"intervalMs":60001}""",
                        """{"prompt":"hi","events":3,"intervalMs":10,"failAt":4}""",
                    )

                    invalidBodies.forEach { body ->
                        val httpReq = HttpRequest.newBuilder()
                            .uri(URI.create("${baseUrl()}/chat/stream"))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(body))
                            .build()
                        val response = httpClient.send(httpReq, HttpResponse.BodyHandlers.ofString())
                        response.statusCode() shouldBe 400
                        val err = objectMapper.readValue<ErrorResponse>(response.body())
                        err.code shouldBe StreamErrorCodes.INVALID_REQUEST
                    }
                }
            }
        }

        given("스트림 중 오류 발생 (failAt)") {
            `when`("events=4, failAt=2 로 요청하면") {
                then("1번 message 후 error(SIMULATED_ERROR) 이벤트를 보내고 done 없이 종료한다") {
                    val req = StreamRequest(prompt = "fail test", events = 4, intervalMs = 10, failAt = 2)
                    val response = postSse(req)
                    val items = readAllSse(response)

                    response.statusCode() shouldBe 200
                    items.filter { it.event == StreamEventNames.MESSAGE } shouldHaveSize 1
                    val errors = items.filter { it.event == StreamEventNames.ERROR }
                    errors shouldHaveSize 1
                    val errPayload = objectMapper.readValue<ErrorEventData>(errors.first().data!!)
                    errPayload.code shouldBe StreamErrorCodes.SIMULATED_ERROR
                    items.filter { it.event == StreamEventNames.DONE } shouldHaveSize 0

                    waitUntilZeroActiveConnections()
                    sseMetrics.activeConnections() shouldBe 0
                }
            }
        }

        given("Heartbeat 전송") {
            `when`("이벤트 간격이 heartbeat 주기보다 길면") {
                then(": ping 주석 행을 수신한다") {
                    val req = StreamRequest(prompt = "heartbeat", events = 2, intervalMs = 350)
                    val response = postSse(req)
                    val items = readAllSse(response)

                    response.statusCode() shouldBe 200
                    val pings = items.filter { it.comment == StreamEventNames.HEARTBEAT_COMMENT }
                    pings.size shouldBeGreaterThanOrEqual 1

                    waitUntilZeroActiveConnections()
                    sseMetrics.activeConnections() shouldBe 0
                }
            }
        }

        given("클라이언트 중도 연결 종료") {
            `when`("스트림 도중 연결을 닫으면") {
                then("서버가 작업을 취소하고 활성 연결 지표가 0으로 복귀한다") {
                    val req = StreamRequest(prompt = "cancel test", events = 50, intervalMs = 80)
                    val response = postSse(req)
                    response.statusCode() shouldBe 200

                    response.body().use { input ->
                        val reader = BufferedReader(InputStreamReader(input, StandardCharsets.UTF_8))
                        var seenMessages = 0
                        while (seenMessages < 2) {
                            val line = reader.readLine() ?: break
                            if (line.startsWith("event:${StreamEventNames.MESSAGE}") ||
                                line.startsWith("event: ${StreamEventNames.MESSAGE}")
                            ) {
                                seenMessages++
                            }
                        }
                    }

                    waitUntilZeroActiveConnections(timeoutMs = 3000)
                    sseMetrics.activeConnections() shouldBe 0
                }
            }
        }

        given("DB 프로파일 미활성화 상태에서 persist=true 요청") {
            `when`("persist=true 로 요청을 보내면") {
                then("DB 저장을 무시하고 정상적으로 스트림을 완료한다") {
                    val req = StreamRequest(prompt = "persist ignored", events = 2, intervalMs = 10, persist = true)
                    val response = postSse(req)
                    val items = readAllSse(response)

                    response.statusCode() shouldBe 200
                    items.filter { it.event == StreamEventNames.MESSAGE } shouldHaveSize 2
                    items.filter { it.event == StreamEventNames.DONE } shouldHaveSize 1

                    waitUntilZeroActiveConnections()
                    sseMetrics.activeConnections() shouldBe 0
                }
            }
        }
    }

    private fun postSse(req: StreamRequest): HttpResponse<java.io.InputStream> {
        val httpReq = HttpRequest.newBuilder()
            .uri(URI.create("${baseUrl()}/chat/stream"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(req)))
            .build()
        return httpClient.send(httpReq, HttpResponse.BodyHandlers.ofInputStream())
    }

    private fun readAllSse(response: HttpResponse<java.io.InputStream>): List<ParsedSseItem> {
        val items = mutableListOf<ParsedSseItem>()
        response.body().use { input ->
            val reader = BufferedReader(InputStreamReader(input, StandardCharsets.UTF_8))
            var currentEvent: String? = null
            val dataBuilder = StringBuilder()

            while (true) {
                val line = reader.readLine() ?: break
                when {
                    line.startsWith(":") -> {
                        val comment = line.removePrefix(":").trim()
                        items.add(ParsedSseItem(comment = comment))
                    }
                    line.startsWith("event:") -> {
                        currentEvent = line.removePrefix("event:").trim()
                    }
                    line.startsWith("data:") -> {
                        if (dataBuilder.isNotEmpty()) dataBuilder.append('\n')
                        dataBuilder.append(line.removePrefix("data:").trim())
                    }
                    line.isEmpty() -> {
                        if (currentEvent != null || dataBuilder.isNotEmpty()) {
                            items.add(
                                ParsedSseItem(
                                    event = currentEvent,
                                    data = dataBuilder.toString(),
                                ),
                            )
                            currentEvent = null
                            dataBuilder.setLength(0)
                        }
                    }
                }
            }
            if (currentEvent != null || dataBuilder.isNotEmpty()) {
                items.add(ParsedSseItem(event = currentEvent, data = dataBuilder.toString()))
            }
        }
        return items
    }

    private fun waitUntilZeroActiveConnections(timeoutMs: Long = 2000) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (sseMetrics.activeConnections() != 0 && System.currentTimeMillis() < deadline) {
            Thread.sleep(30)
        }
    }
}
