package jhkim105.tutorials.redis.streams

import java.time.Duration
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
 * Redis Streams 장점 #3: 메시지 재처리 (PENDING + XCLAIM)
 *
 * Pub/Sub 과의 차이:
 * - Pub/Sub: Consumer가 처리 실패해도 메시지 유실 → 재처리 불가
 * - Streams:
 * - Consumer가 읽는 순간 PENDING 상태로 전환
 * - 처리 완료 시 XACK로 PENDING에서 제거
 * - ACK 없이 일정 시간 경과 시 XCLAIM으로 다른 Consumer가 인수하여 재처리
 */
@SpringBootTest
class StreamRetryTest
@Autowired
constructor(private val redisTemplate: RedisTemplate<String, String>) {
        private val testStreamKey = "test:stream:retry"
        private val groupName = "retry-group"

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
        fun `처리에 실패한 ACK 없는 메시지는 PENDING 상태로 남는다`() {
                val ops = redisTemplate.opsForStream<String, String>()

                ops.add(MapRecord.create(testStreamKey, mapOf("msg" to "important message")))

                // consumer-1이 읽기만 하고 ACK 안 함 (처리 실패 시뮬레이션)
                val received =
                        ops.read(
                                        Consumer.from(groupName, "consumer-1"),
                                        StreamOffset.create(
                                                testStreamKey,
                                                ReadOffset.lastConsumed()
                                        )
                                )
                                .orEmpty()
                assertEquals(1, received.size)
                println("consumer-1이 메시지를 읽었지만 ACK를 보내지 않음")

                // PENDING 목록 확인
                val pending = ops.pending(testStreamKey, groupName)!!
                assertTrue(pending.totalPendingMessages > 0)

                println("✅ PENDING 메시지 수: ${pending.totalPendingMessages}")
                println("   → Pub/Sub이라면 이 메시지는 이미 소멸되었을 것입니다.")
        }

        @Test
        fun `PENDING 상태의 메시지를 다른 Consumer가 XCLAIM으로 인수하여 재처리할 수 있다`() {
                val ops = redisTemplate.opsForStream<String, String>()

                ops.add(MapRecord.create(testStreamKey, mapOf("msg" to "recoverable message")))

                // consumer-1이 읽고 ACK 안 함
                val received =
                        ops.read(
                                        Consumer.from(groupName, "consumer-1"),
                                        StreamOffset.create(
                                                testStreamKey,
                                                ReadOffset.lastConsumed()
                                        )
                                )
                                .orEmpty()
                val failedMessageId = received.first().id

                // 즉시 consumer-2가 XCLAIM으로 메시지 인수
                val claimed =
                        ops.claim(
                                        testStreamKey,
                                        groupName,
                                        "consumer-2",
                                        Duration.ofMillis(0), // 즉시 클레임 (실제 환경에서는 충분한 대기 시간 설정)
                                        failedMessageId
                                )
                                .orEmpty()
                assertEquals(1, claimed.size)
                println("✅ consumer-2가 consumer-1의 PENDING 메시지를 XCLAIM으로 인수 완료")
                println("   메시지: ${claimed.first().value}")

                // consumer-2가 재처리 완료 후 ACK
                ops.acknowledge(testStreamKey, groupName, claimed.first().id)

                // PENDING 목록에서 제거되었는지 확인
                val afterAck = ops.pending(testStreamKey, groupName)!!
                assertTrue(afterAck.totalPendingMessages <= 0)
                println("✅ ACK 후 PENDING 메시지 수: ${afterAck.totalPendingMessages} (처리 완료)")
        }

        @Test
        fun `XACK로 처리 완료된 메시지는 PENDING에서 제거되지만 스트림 자체에서는 유지된다`() {
                val ops = redisTemplate.opsForStream<String, String>()

                ops.add(MapRecord.create(testStreamKey, mapOf("msg" to "ack test")))

                val received =
                        ops.read(
                                        Consumer.from(groupName, "consumer-1"),
                                        StreamOffset.create(
                                                testStreamKey,
                                                ReadOffset.lastConsumed()
                                        )
                                )
                                .orEmpty()
                val msgId = received.first().id

                // ACK 전 PENDING 확인
                val beforeAck = ops.pending(testStreamKey, groupName)!!
                assertTrue(beforeAck.totalPendingMessages > 0)

                // ACK 처리
                ops.acknowledge(testStreamKey, groupName, msgId)

                // ACK 후 PENDING에서 제거
                val afterAck = ops.pending(testStreamKey, groupName)!!
                assertTrue(afterAck.totalPendingMessages <= 0)

                // 하지만 스트림 자체에는 메시지가 남아 있음 (XDEL 하지 않는 한)
                val stillInStream = ops.read(StreamOffset.fromStart(testStreamKey)).orEmpty()
                assertEquals(1, stillInStream.size)
                println(
                        "✅ ACK 후 PENDING: ${afterAck.totalPendingMessages}개, 스트림: ${stillInStream.size}개 유지"
                )
        }
}
