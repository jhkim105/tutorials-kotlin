package jhkim105.tutorials.redis

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import kotlin.test.Test


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RedisListTest {

    private lateinit var redisTemplate: ReactiveRedisTemplate<String, String>

    @BeforeAll
    fun setup() {
        val factory: ReactiveRedisConnectionFactory = LettuceConnectionFactory("localhost", 6379).apply { afterPropertiesSet() }
        redisTemplate = ReactiveStringRedisTemplate(factory)
    }

    @BeforeEach
    fun clearRedis(): Unit = runBlocking {
        redisTemplate.delete("testList").block()
    }

    @Test
    fun `LPUSH should add items to the left of the list`():Unit = runBlocking {
        redisTemplate.opsForList().leftPushAll("testList", "A", "B", "C").block()

        val result = redisTemplate.opsForList().range("testList", 0, -1).collectList().block()

        assertEquals(listOf("C", "B", "A"), result)
    }

    @Test
    fun `RPUSH should add items to the right of the list`() = runBlocking {
        redisTemplate.opsForList().rightPushAll("testList", "A", "B", "C").block()

        val result = redisTemplate.opsForList().range("testList", 0, -1).collectList().block()

        Assertions.assertEquals(listOf("A", "B", "C"), result)
    }

    @Test
    fun `LRANGE should return correct pagination results`() = runBlocking {
        redisTemplate.opsForList().rightPushAll("testList", "A", "B", "C", "D", "E").block()

        val page1 = redisTemplate.opsForList().range("testList", 0, 2).collectList().block()
        val page2 = redisTemplate.opsForList().range("testList", 3, 4).collectList().block()

        Assertions.assertEquals(listOf("A", "B", "C"), page1)
        Assertions.assertEquals(listOf("D", "E"), page2)
    }

    @Test
    fun `Deleting list should remove all elements`() = runBlocking {
        redisTemplate.opsForList().rightPushAll("testList", "A", "B", "C").block()

        redisTemplate.delete("testList").block()

        val result = redisTemplate.opsForList().range("testList", 0, -1).collectList().block()
        Assertions.assertTrue(result?.isEmpty() == true)
    }
}