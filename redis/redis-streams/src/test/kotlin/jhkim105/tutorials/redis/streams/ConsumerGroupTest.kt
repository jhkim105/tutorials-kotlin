package jhkim105.tutorials.redis.streams

import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.connection.stream.Consumer
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.connection.stream.ReadOffset
import org.springframework.data.redis.connection.stream.StreamOffset
import org.springframework.data.redis.core.RedisTemplate

/**
 * Redis Streams 장점 #2: Consumer Group 기반 분산 처리
 *
 * Pub/Sub 과의 차이:
 * - Pub/Sub: 모든 구독자가 동일 메시지를 수신(브로드캐스트) → 중복 처리 발생
 * - Streams: Consumer Group 내에서 각 메시지는 정확히 하나의 Consumer에게만 전달됨
 * ```
 *            → 중복 없이 병렬 처리 가능
 * ```
 */
@SpringBootTest
class ConsumerGroupTest
@Autowired
constructor(private val redisTemplate: RedisTemplate<String, String>) {
    private val testStreamKey = "test:stream:consumer-group"
    private val groupName = "test-group"

    @BeforeEach
    fun setUp() {
        redisTemplate.delete(testStreamKey)
        try {
            redisTemplate
                    .opsForStream<String, String>()
                    .createGroup(testStreamKey, ReadOffset.from("0"), groupName)
        } catch (e: Exception) {
            /* 이미 존재하면 무시 */
        }
    }

    @AfterEach
    fun tearDown() {
        redisTemplate.delete(testStreamKey)
    }

    @Test
    fun `동일 Consumer Group 내 두 Consumer는 메시지를 중복 없이 분산 처리한다`() {
        val ops = redisTemplate.opsForStream<String, String>()

        // 메시지 10개 발행
        repeat(10) { i ->
            ops.add(MapRecord.create(testStreamKey, mapOf("seq" to "$i", "msg" to "message-$i")))
        }

        // consumer-1, consumer-2 각각 독립적으로 읽기
        val consumer1Messages =
                ops.read(
                                Consumer.from(groupName, "consumer-1"),
                                StreamOffset.create(testStreamKey, ReadOffset.lastConsumed())
                        )
                        .orEmpty()
        val consumer2Messages =
                ops.read(
                                Consumer.from(groupName, "consumer-2"),
                                StreamOffset.create(testStreamKey, ReadOffset.lastConsumed())
                        )
                        .orEmpty()

        val ids1 = consumer1Messages.map { it.id.toString() }.toSet()
        val ids2 = consumer2Messages.map { it.id.toString() }.toSet()

        // 두 Consumer가 읽은 메시지의 교집합은 0이어야 함 (중복 없음)
        val intersection = ids1.intersect(ids2)
        assertTrue(intersection.isEmpty(), "두 Consumer가 동일 메시지를 처리해서는 안 됩니다")

        // 두 Consumer가 읽은 메시지 합계가 전체 메시지 수와 일치해야 함
        assertEquals(10, ids1.size + ids2.size)

        println("✅ Consumer Group 분산 처리 결과:")
        println("  consumer-1: ${ids1.size}개 처리")
        println("  consumer-2: ${ids2.size}개 처리")
        println("  중복 메시지: 0개 | 총 처리: ${ids1.size + ids2.size}개 / 10개")
    }

    @Test
    fun `Consumer Group 없이 독립 읽기 시 모든 Consumer가 동일 메시지를 받는다(브로드캐스트)`() {
        val ops = redisTemplate.opsForStream<String, String>()

        // 메시지 3개 발행
        repeat(3) { i -> ops.add(MapRecord.create(testStreamKey, mapOf("seq" to "$i"))) }

        // 그룹 없이 독립 Consumer로 읽기 (0-0 = 처음부터)
        val reader1 = ops.read(StreamOffset.fromStart(testStreamKey)).orEmpty()
        val reader2 = ops.read(StreamOffset.fromStart(testStreamKey)).orEmpty()

        // 그룹 없이 읽으면 두 Consumer 모두 동일 메시지 수신 (브로드캐스트)
        assertEquals(3, reader1.size)
        assertEquals(3, reader2.size)

        println("⚠️ 그룹 없는 독립 읽기(브로드캐스트):")
        println("  reader-1: ${reader1.size}개 수신 (중복)")
        println("  reader-2: ${reader2.size}개 수신 (중복)")
        println("  → Consumer Group 사용 시 각 메시지를 한 번만 처리 가능")
    }
}
