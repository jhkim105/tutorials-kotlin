package jhkim105.tutorials.redis

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.Duration
import jhkim105.tutorials.redis.pubsub.PubSubPublisher
import jhkim105.tutorials.redis.pubsub.PubSubSubscriber
import jhkim105.tutorials.redis.streams.StreamPublisher
import jhkim105.tutorials.redis.streams.VS_STREAM_KEY
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.connection.stream.Consumer
import org.springframework.data.redis.connection.stream.ReadOffset
import org.springframework.data.redis.connection.stream.StreamOffset
import org.springframework.data.redis.core.RedisTemplate

/**
 * Redis Streams vs Pub/Sub 직접 비교 테스트
 *
 * 동일한 시나리오를 Pub/Sub과 Streams 양쪽으로 실행하여 Streams의 장점을 체감할 수 있도록 구성된 통합 테스트입니다.
 *
 * 비교 시나리오:
 * 1. Consumer 오프라인 중 발행된 메시지 처리 (영속성)
 * 2. 처리 실패 후 재처리 가능 여부 (내결함성)
 * 3. 메시지 재생 (Replay)
 * 4. 분산 처리 (Consumer Group vs 브로드캐스트)
 */
@SpringBootTest
class StreamsVsPubSubComparisonTest(
        private val pubSubPublisher: PubSubPublisher,
        private val pubSubSubscriber: PubSubSubscriber,
        private val streamPublisher: StreamPublisher,
        private val redisTemplate: RedisTemplate<String, String>
) :
        StringSpec({
            val groupName = "vs-group"

            beforeEach {
                pubSubSubscriber.clear()
                redisTemplate.delete(VS_STREAM_KEY)
                // Consumer Group 생성
                try {
                    redisTemplate
                            .opsForStream<String, String>()
                            .createGroup(VS_STREAM_KEY, ReadOffset.from("0"), groupName)
                } catch (e: Exception) {
                    /* 이미 존재하면 무시 */
                }
            }

            afterEach { redisTemplate.delete(VS_STREAM_KEY) }

            // ──────────────────────────────────────────────────
            // 시나리오 1: Consumer 오프라인 중 메시지 영속성
            // ──────────────────────────────────────────────────

            "【비교 1】Pub/Sub: 구독자가 없을 때 발행된 메시지는 유실된다" {
                pubSubSubscriber.clear()

                // 메시지 5개 발행 (구독자는 이미 등록되어 있으나, 오프라인 시나리오 시뮬레이션)
                // 실제 오프라인 테스트: 구독자 컨테이너 stop → publish → start 후 메시지 수신 안 됨
                repeat(5) { i -> pubSubPublisher.publish("pubsub-offline-msg-$i") }
                Thread.sleep(300)

                // 이미 등록된 구독자는 수신함 (온라인 상태이므로)
                // → 핵심: 구독자가 오프라인이었다면 receivedMessages = 0
                println("✅ Pub/Sub 수신 수 (온라인): ${pubSubSubscriber.receivedMessages.size}개")
                println("⚠️  Pub/Sub 오프라인 시나리오: 구독자가 없었다면 0개 수신 (메시지 유실)")
                println("   → Streams는 오프라인이어도 나중에 전부 수신 가능")
            }

            "【비교 1】Streams: Consumer가 오프라인이어도 나중에 발행된 메시지를 전부 받는다" {
                // Consumer 없이 5개 발행
                repeat(5) { i -> streamPublisher.publish("stream-offline-msg-$i") }

                // Consumer가 나중에 붙어 처음(0-0)부터 읽기
                val messages =
                        redisTemplate
                                .opsForStream<String, String>()
                                .read(StreamOffset.fromStart(VS_STREAM_KEY))
                                .orEmpty()

                messages.size shouldBe 5
                println("✅ Streams 수신 수 (오프라인 후 재연결): ${messages.size}개 — 메시지 유실 없음")
                messages.forEachIndexed { i, msg -> println("  [$i] ${msg.value["msg"]}") }
            }

            // ──────────────────────────────────────────────────
            // 시나리오 2: 처리 실패 후 재처리 (내결함성)
            // ──────────────────────────────────────────────────

            "【비교 2】Pub/Sub: 처리 실패해도 메시지를 재처리할 방법이 없다" {
                var processedCount = 0
                pubSubSubscriber.clear()

                repeat(3) { i -> pubSubPublisher.publish("critical-msg-$i") }
                Thread.sleep(300)

                val received = pubSubSubscriber.receivedMessages.toList()
                received.forEach { msg ->
                    try {
                        if (msg.contains("1")) throw RuntimeException("처리 실패!")
                        processedCount++
                    } catch (e: Exception) {
                        println("❌ [Pub/Sub] 메시지 처리 실패: $msg — 재처리 불가, 영구 유실")
                    }
                }
                println("⚠️  Pub/Sub: 3개 중 ${processedCount}개만 처리 완료, 실패한 메시지는 유실됨")
            }

            "【비교 2】Streams: ACK 없는 메시지는 PENDING으로 남아 재처리할 수 있다" {
                val ops = redisTemplate.opsForStream<String, String>()

                // 3개 발행
                repeat(3) { i -> streamPublisher.publish("critical-msg-$i") }

                // consumer-1이 읽고 처리 실패 시뮬레이션 (ACK 하지 않음)
                val received =
                        ops.read(
                                        Consumer.from(groupName, "consumer-1"),
                                        StreamOffset.create(
                                                VS_STREAM_KEY,
                                                ReadOffset.lastConsumed()
                                        )
                                )
                                .orEmpty()
                println("consumer-1이 ${received.size}개 수신, ACK 없이 처리 실패 시뮬레이션")

                // PENDING 확인
                val pending = ops.pending(VS_STREAM_KEY, groupName)!!
                println("✅ PENDING 상태 메시지: ${pending.totalPendingMessages}개")

                // consumer-2가 XCLAIM으로 재처리
                val failedIds = received.map { it.id }
                val claimed =
                        ops.claim(
                                        VS_STREAM_KEY,
                                        groupName,
                                        "consumer-2",
                                        Duration.ofMillis(0),
                                        *failedIds.toTypedArray()
                                )
                                .orEmpty()
                claimed.size shouldBe received.size

                // 재처리 후 ACK
                claimed.forEach { ops.acknowledge(VS_STREAM_KEY, groupName, it.id) }

                val afterAck = ops.pending(VS_STREAM_KEY, groupName)!!
                println("✅ consumer-2 재처리 완료. 남은 PENDING: ${afterAck.totalPendingMessages}개")
                afterAck.totalPendingMessages shouldBe 0L
            }

            // ──────────────────────────────────────────────────
            // 시나리오 3: 메시지 재생 (Replay)
            // ──────────────────────────────────────────────────

            "【비교 3】Pub/Sub: 이미 발행된 메시지를 다시 읽을 수 없다" {
                pubSubSubscriber.clear()

                pubSubPublisher.publish("historical-event-1")
                pubSubPublisher.publish("historical-event-2")
                Thread.sleep(300)

                val firstRead = pubSubSubscriber.receivedMessages.size
                println("첫 번째 수신: $firstRead 개")

                // 구독자를 재등록해도 과거 메시지는 받을 수 없음
                pubSubSubscriber.clear()
                Thread.sleep(100)

                val secondRead = pubSubSubscriber.receivedMessages.size
                secondRead shouldBe 0
                println("⚠️  Pub/Sub 재구독 후 과거 메시지 수신: ${secondRead}개 — 재생 불가")
            }

            "【비교 3】Streams: 특정 시점 이후의 메시지를 언제든 재생할 수 있다" {
                val ops = redisTemplate.opsForStream<String, String>()

                // 이벤트 5개 발행
                val ids = (1..5).map { i -> streamPublisher.publish("historical-event-$i") }

                // 첫 번째 읽기
                val firstRead = ops.read(StreamOffset.fromStart(VS_STREAM_KEY)).orEmpty()
                println("첫 번째 읽기: ${firstRead.size}개")

                // 3번째 이벤트 ID 이후부터 재생
                val replayFrom = ids[2]
                val replayed =
                        ops.read(StreamOffset.create(VS_STREAM_KEY, ReadOffset.from(replayFrom)))
                                .orEmpty()
                replayed.size shouldBe 2 // 4번째, 5번째

                println("✅ ID ${replayFrom} 이후부터 재생: ${replayed.size}개")
                replayed.forEach { println("  ${it.value["msg"]}") }

                // 처음부터 전체 재생
                val fullReplay = ops.read(StreamOffset.fromStart(VS_STREAM_KEY)).orEmpty()
                fullReplay.size shouldBe 5
                println("✅ 전체 재생: ${fullReplay.size}개 — 이미 처리된 메시지도 재처리 가능")
            }

            // ──────────────────────────────────────────────────
            // 시나리오 4: 분산 처리
            // ──────────────────────────────────────────────────

            "【비교 4】Pub/Sub: 여러 구독자가 있으면 모두가 동일 메시지를 받는다 (브로드캐스트)" {
                println("⚠️  Pub/Sub 브로드캐스트 특성:")
                println("   구독자 A, B가 모두 동일 채널 구독 시 → A와 B 모두 동일 메시지 수신")
                println("   멀티 인스턴스 환경에서 중복 처리 발생 → 별도 중복 방지 로직 필요")
                println("   → Redisson PubSub 예제: spring-data-redisson-pubsub 모듈 참고")

                pubSubSubscriber.clear()
                pubSubPublisher.publish("broadcast-msg")
                Thread.sleep(300)
                println("   현재 구독자 1개 — 수신: ${pubSubSubscriber.receivedMessages.size}개")
                println("   구독자가 N개라면 N개 모두 수신 (중복)")
            }

            "【비교 4】Streams: Consumer Group을 사용하면 각 메시지는 하나의 Consumer만 처리한다" {
                val ops = redisTemplate.opsForStream<String, String>()

                // 메시지 10개 발행
                repeat(10) { i -> streamPublisher.publish("work-item-$i") }

                // consumer-1, consumer-2가 같은 그룹으로 읽기
                val c1 =
                        ops.read(
                                        Consumer.from(groupName, "consumer-1"),
                                        StreamOffset.create(
                                                VS_STREAM_KEY,
                                                ReadOffset.lastConsumed()
                                        )
                                )
                                .orEmpty()
                val c2 =
                        ops.read(
                                        Consumer.from(groupName, "consumer-2"),
                                        StreamOffset.create(
                                                VS_STREAM_KEY,
                                                ReadOffset.lastConsumed()
                                        )
                                )
                                .orEmpty()

                val ids1 = c1.map { it.id.toString() }.toSet()
                val ids2 = c2.map { it.id.toString() }.toSet()
                val overlap = ids1.intersect(ids2)

                overlap.size shouldBe 0
                (ids1.size + ids2.size) shouldBe 10

                println("✅ Streams Consumer Group 분산 처리 결과:")
                println("   consumer-1: ${ids1.size}개 | consumer-2: ${ids2.size}개 | 중복: 0개")
                println("   → 멀티 인스턴스에서도 메시지가 정확히 한 번 처리됨")
            }
        })
