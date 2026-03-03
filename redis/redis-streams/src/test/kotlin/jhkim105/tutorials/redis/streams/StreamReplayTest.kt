package jhkim105.tutorials.redis.streams

import kotlin.test.assertEquals
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.connection.stream.ReadOffset
import org.springframework.data.redis.connection.stream.RecordId
import org.springframework.data.redis.connection.stream.StreamOffset
import org.springframework.data.redis.core.RedisTemplate

/**
 * Redis Streams 장점 #4: 메시지 재생 (Replay)
 *
 * Pub/Sub 과의 차이:
 * - Pub/Sub: 이미 발행된 메시지는 다시 읽을 수 없음
 * - Streams: 특정 메시지 ID 이후부터 재생하거나, 처음부터 전체 재생 가능
 * ```
 *            → 신규 서비스 배포 시 과거 이벤트를 재처리하는 Event Sourcing 패턴에 활용
 * ```
 */
@SpringBootTest
class StreamReplayTest
@Autowired
constructor(private val redisTemplate: RedisTemplate<String, String>) {
    private val testStreamKey = "test:stream:replay"

    @BeforeEach
    fun setUp() {
        redisTemplate.delete(testStreamKey)
    }

    @AfterEach
    fun tearDown() {
        redisTemplate.delete(testStreamKey)
    }

    @Test
    fun `처음부터 전체 메시지를 재생 Replay 할 수 있다`() {
        val ops = redisTemplate.opsForStream<String, String>()

        // 메시지 5개 발행
        repeat(5) { i ->
            ops.add(
                    MapRecord.create(
                            testStreamKey,
                            mapOf("seq" to "$i", "event" to "order-created-$i")
                    )
            )
        }

        // 이미 한 번 읽었다고 가정
        ops.read(StreamOffset.fromStart(testStreamKey))

        // 처음부터 다시 재생
        val replayed = ops.read(StreamOffset.fromStart(testStreamKey)).orEmpty()
        assertEquals(5, replayed.size)

        println("✅ 전체 재생(Replay) 결과: ${replayed.size}개 메시지를 처음부터 다시 읽었습니다.")
        replayed.forEachIndexed { i, msg -> println("  [$i] ${msg.value}") }
    }

    @Test
    fun `특정 ID 이후의 메시지만 재생할 수 있다`() {
        val ops = redisTemplate.opsForStream<String, String>()

        // 메시지 5개 발행 후 ID 수집
        val ids = mutableListOf<RecordId>()
        repeat(5) { i ->
            val id = ops.add(MapRecord.create(testStreamKey, mapOf("seq" to "$i")))
            ids.add(id!!)
        }

        // 3번째 메시지(index=2) ID 이후부터 재생
        val fromId = ids[2]
        val replayed =
                ops.read(StreamOffset.create(testStreamKey, ReadOffset.from(fromId))).orEmpty()

        // 3번째 이후: 4번째, 5번째 = 2개
        assertEquals(2, replayed.size)
        println("✅ ID ${fromId} 이후부터 재생: ${replayed.size}개 수신")
        replayed.forEach { println("  ${it.id} → ${it.value}") }
    }

    @Test
    fun `특정 index 시점부터 포함하여 재생하려면 이전 ID를 기준점으로 사용한다`() {
        val ops = redisTemplate.opsForStream<String, String>()

        val ids = mutableListOf<RecordId>()
        repeat(5) { i ->
            val id =
                    ops.add(
                            MapRecord.create(
                                    testStreamKey,
                                    mapOf("seq" to "$i", "event" to "event-$i")
                            )
                    )
            ids.add(id!!)
        }

        // 4번째(index=3) 메시지부터 포함하여 재생하려면 3번째(index=2) ID를 기준으로 사용
        val fromId = ids[2]
        val replayed =
                ops.read(StreamOffset.create(testStreamKey, ReadOffset.from(fromId))).orEmpty()

        // index=3, index=4 메시지 = 2개
        assertEquals(2, replayed.size)
        assertEquals("3", replayed.first().value["seq"])

        println("✅ index=3 메시지부터 재생:")
        replayed.forEach { println("  seq=${it.value["seq"]}, event=${it.value["event"]}") }
    }

    @Test
    fun `최신 N개 메시지를 가져올 수 있다`() {
        val ops = redisTemplate.opsForStream<String, String>()

        repeat(10) { i -> ops.add(MapRecord.create(testStreamKey, mapOf("seq" to "$i"))) }

        // 전체 읽기 후 마지막 3개
        val latest = ops.read(StreamOffset.fromStart(testStreamKey)).orEmpty().takeLast(3)

        assertEquals(3, latest.size)
        assertEquals("9", latest.last().value["seq"])

        println("✅ 전체 10개 중 최신 3개:")
        latest.forEach { println("  seq=${it.value["seq"]}") }
    }
}
