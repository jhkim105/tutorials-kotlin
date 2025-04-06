package jhkim105.tutorials.redis.streams

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service


@Service
class RedisMessageProducer (
    private val redisTemplate: RedisTemplate<String, String>,
    private val objectMapper: ObjectMapper
) {

    fun sendToStream(streamKey:String, message: String) {
        val map = objectMapper.readValue<Map<String, String>>(message)
        val record = MapRecord.create(streamKey, map)
        redisTemplate.opsForStream<String, String>().add(record)
    }
}