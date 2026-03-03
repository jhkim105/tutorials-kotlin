package jhkim105.tutorials.redis.streams

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.connection.stream.RecordId
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

const val VS_STREAM_KEY = "vs:stream:channel"

@Service
class StreamPublisher(private val redisTemplate: RedisTemplate<String, String>) {
    fun publish(message: String): RecordId {
        val record = MapRecord.create(VS_STREAM_KEY, mapOf("msg" to message))
        val id = redisTemplate.opsForStream<String, String>().add(record)!!
        log.info { "📤 [Streams] Published to $VS_STREAM_KEY: $message (id=$id)" }
        return id
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}
