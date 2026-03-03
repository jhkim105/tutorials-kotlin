package jhkim105.tutorials.redis.streams

import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.connection.stream.StreamOffset
import org.springframework.data.redis.core.RedisTemplate

/**
 * Redis Streams 장점 #1: 메시지 영속성(Persistence)
 *
 * Pub/Sub 과의 차이:
 * - Pub/Sub: 구독자가 없을 때 발행된 메시지는 영구 유실됨
 * - Streams: 메시지가 로그에 저장되므로 나중에 Consumer가 붙어도 모든 메시지 수신 가능
 */
@SpringBootTest
class StreamPersistenceTest
@Autowired
constructor(private val redisTemplate: RedisTemplate<String, String>) {
    private val testStreamKey = "test:stream:persistence"

    @BeforeEach
    fun setUp() {
        redisTemplate.delete(testStreamKey)
    }

    @AfterEach
    fun tearDown() {
        redisTemplate.delete(testStreamKey)
    }

    @Test
    fun `Consumer 없이 발행한 메시지가 스트림에 영속적으로 저장된다`() {
        val ops = redisTemplate.opsForStream<String, String>()

        // Consumer 없이 메시지 5개 발행
        repeat(5) { i ->
            ops.add(MapRecord.create(testStreamKey, mapOf("id" to "$i", "msg" to "message-$i")))
        }

        // Consumer가 나중에 붙어서 처음부터(0-0) 전부 읽음
        val messages = ops.read(StreamOffset.fromStart(testStreamKey)).orEmpty()

        assertEquals(5, messages.size)
        println("✅ Consumer 없이 발행된 메시지 ${messages.size}개를 나중에 모두 수신했습니다.")
        messages.forEachIndexed { i, msg -> println("  [$i] ${msg.value}") }
    }

    @Test
    fun `메시지는 Consumer가 읽은 후에도 삭제되지 않고 스트림에 유지된다`() {
        val ops = redisTemplate.opsForStream<String, String>()

        ops.add(MapRecord.create(testStreamKey, mapOf("msg" to "persistent message")))

        // 첫 번째 읽기
        val firstRead = ops.read(StreamOffset.fromStart(testStreamKey)).orEmpty()
        assertEquals(1, firstRead.size)

        // 두 번째 읽기 — 메시지가 그대로 남아 있음
        val secondRead = ops.read(StreamOffset.fromStart(testStreamKey)).orEmpty()
        assertEquals(1, secondRead.size)

        println("✅ 메시지를 읽은 후에도 스트림에 ${secondRead.size}개의 메시지가 유지됩니다.")
    }

    @Test
    fun `스트림에 쌓인 메시지 수 XLEN 를 확인할 수 있다`() {
        val ops = redisTemplate.opsForStream<String, String>()

        repeat(10) { i -> ops.add(MapRecord.create(testStreamKey, mapOf("seq" to "$i"))) }

        val streamLen = ops.size(testStreamKey) ?: 0
        assertTrue(streamLen > 0)
        println("✅ 스트림에 쌓인 메시지 수(XLEN): $streamLen 개")
    }
}
