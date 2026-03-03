package jhkim105.tutorials.redis.streams

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import java.time.Instant
import org.springframework.data.redis.connection.stream.Consumer
import org.springframework.data.redis.connection.stream.ReadOffset
import org.springframework.data.redis.connection.stream.RecordId
import org.springframework.data.redis.connection.stream.StreamOffset
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Redis Streams 데모 컨트롤러
 *
 * - POST /streams/produce : 스트림에 메시지(작업) 추가
 * - GET /streams/consume : 특정 ID 이후의 메시지를 Consumer Group으로 읽기 (PENDING 전환)
 * - POST /streams/ack : Consumer Group에서 메시지 처리 완료(ACK) 신호 전송
 *
 * Streams 특성:
 * - 메시지가 로그에 저장되어 Consumer 오프라인 시에도 유실 없음
 * - Consumer Group으로 분산 처리 가능
 * - ACK 전까지 PENDING 상태를 유지하여 재처리 보장
 */
@RestController
@RequestMapping("/streams")
class StreamsController(
        private val streamPublisher: StreamPublisher,
        private val redisTemplate: RedisTemplate<String, String>
) {
    private val groupName = "api-group"
    private val consumerName = "api-consumer"

    @PostConstruct
    fun initConsumerGroup() {
        runCatching {
            // mkStream=true: 스트림이 없으면 자동 생성
            redisTemplate
                    .opsForStream<String, String>()
                    .createGroup(VS_STREAM_KEY, ReadOffset.from("0"), groupName)
            log.info { "Consumer Group '$groupName' created for stream '$VS_STREAM_KEY'" }
        }
                .onFailure { log.info { "Consumer Group '$groupName' already exists (ignored)" } }
    }

    /**
     * 스트림에 메시지 추가 (XADD)
     *
     * 반환된 messageId는 consume / ack 요청에서 fromId / messageId 파라미터로 사용합니다.
     */
    @PostMapping("/produce")
    fun produce(@RequestBody req: ProduceRequest): ProduceResponse {
        val messageId = streamPublisher.publish(req.message)
        log.info { "POST /streams/produce message=${req.message} → id=$messageId" }
        return ProduceResponse(
                messageId = messageId.toString(),
                streamKey = VS_STREAM_KEY,
                message = req.message,
                producedAt = Instant.now().toString()
        )
    }

    /**
     * 스트림에서 메시지 읽기 (Consumer Group 기반, XREADGROUP)
     *
     * @param fromId 읽기 시작 기준 ID
     * - ">" (기본값): 아직 어떤 Consumer에게도 전달되지 않은 새 메시지만 읽기
     * - "0" : 이 Consumer에게 이미 전달됐지만 ACK 안 된 PENDING 메시지부터 읽기
     * - "1234-0" : 특정 메시지 ID 이후부터 읽기
     *
     * 읽힌 메시지는 PENDING 상태로 전환됩니다. 처리 완료 후 POST /streams/ack 로 ACK 해야 PENDING에서 제거됩니다.
     */
    @GetMapping("/consume")
    fun consume(@RequestParam(defaultValue = ">") fromId: String): ConsumeResponse {
        val ops = redisTemplate.opsForStream<String, String>()
        val offset =
                when (fromId) {
                    ">" -> ReadOffset.lastConsumed()
                    else -> ReadOffset.from(fromId)
                }

        val messages =
                ops.read(
                                Consumer.from(groupName, consumerName),
                                StreamOffset.create(VS_STREAM_KEY, offset)
                        )
                        .orEmpty()

        val pending = ops.pending(VS_STREAM_KEY, groupName)?.totalPendingMessages ?: 0

        log.info { "GET /streams/consume fromId=$fromId → ${messages.size}개 수신, PENDING=$pending" }

        return ConsumeResponse(
                count = messages.size,
                pendingCount = pending,
                messages =
                        messages.map { msg ->
                            StreamMessage(messageId = msg.id.toString(), data = msg.value)
                        }
        )
    }

    /**
     * 메시지 처리 완료 신호 (XACK)
     *
     * PENDING 상태의 메시지를 ACK 처리하여 완료로 표시합니다. ACK 후에는 PENDING 목록에서 제거되며, 다른 Consumer가 재처리하지 않습니다. (단,
     * 스트림 로그에서는 삭제되지 않으므로 Replay는 여전히 가능)
     */
    @PostMapping("/ack")
    fun ack(@RequestBody req: AckRequest): AckResponse {
        val ops = redisTemplate.opsForStream<String, String>()
        val ackedCount = ops.acknowledge(VS_STREAM_KEY, groupName, RecordId.of(req.messageId)) ?: 0L
        val pending = ops.pending(VS_STREAM_KEY, groupName)?.totalPendingMessages ?: 0

        log.info {
            "POST /streams/ack messageId=${req.messageId} → ackedCount=$ackedCount, remainPending=$pending"
        }

        return AckResponse(
                messageId = req.messageId,
                group = groupName,
                ackedCount = ackedCount,
                remainPendingCount = pending
        )
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}

data class ProduceRequest(val message: String)

data class ProduceResponse(
        val messageId: String,
        val streamKey: String,
        val message: String,
        val producedAt: String
)

data class ConsumeResponse(
        val count: Int,
        val pendingCount: Long,
        val messages: List<StreamMessage>
)

data class StreamMessage(val messageId: String, val data: Map<String, String>)

data class AckRequest(val messageId: String)

data class AckResponse(
        val messageId: String,
        val group: String,
        val ackedCount: Long,
        val remainPendingCount: Long
)
