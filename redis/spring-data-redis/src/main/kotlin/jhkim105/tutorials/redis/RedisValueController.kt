package jhkim105.tutorials.redis

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/redis/value")
class RedisValueController(
    private val redisTemplate: RedisTemplate<String, String>
) {
    @PostMapping
    fun setValue(key: String, value: String): String {
        redisTemplate.opsForValue().set(key, value)
        return "Saved: $key -> ${redisTemplate.opsForValue().get(key)}"
    }

    @GetMapping
    fun getValue(key: String): String {
        val value = redisTemplate.opsForValue().get(key)
        return "Value: $value"
    }
}