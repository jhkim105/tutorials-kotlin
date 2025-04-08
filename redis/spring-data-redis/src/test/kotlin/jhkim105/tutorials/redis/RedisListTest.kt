package jhkim105.tutorials.redis

import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import java.util.concurrent.TimeUnit
import kotlin.test.Test

@SpringBootTest
class RedisListTest @Autowired constructor(
    private val redisTemplate: RedisTemplate<String, String>
) {


    @Test
    fun leftPush() {
        redisTemplate.opsForList().leftPush("list1", "data1")
        redisTemplate.opsForList().leftPush("list1", "data2")
        redisTemplate.expire("list1", 60, TimeUnit.SECONDS)
        val result = redisTemplate.opsForList().range("list1", 0, 1)
        println(result)
        result shouldBe listOf("data2", "data1")
    }

    @Test
    fun rightPush() {
        redisTemplate.opsForList().rightPush("list2", "data1")
        redisTemplate.opsForList().rightPush("list2", "data2")
        redisTemplate.expire("list2", 60, TimeUnit.SECONDS)
        val result = redisTemplate.opsForList().range("list2", 0, 1)
        result shouldBe listOf("data1", "data2")
    }
}

