package jhkim105.tutorials.redis

import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.SetOperations
import java.util.concurrent.TimeUnit
import kotlin.test.Test

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RedisSetTest @Autowired constructor(
    private val redisTemplate: RedisTemplate<String, String>
) {

    private lateinit var setOps: SetOperations<String, String>
    private val redisKey = "test:set"

    @BeforeAll
    fun setup() {
        setOps = redisTemplate.opsForSet()
    }

    @BeforeEach
    fun clearRedisSet() {
        redisTemplate.delete(redisKey)
    }

    @Test
    fun add() {
        setOps.add(redisKey, "value1", "value2")
        val members = setOps.members(redisKey)

        members?.size shouldBe 2
        members?.contains("value1") shouldBe true
        members?.contains("value2") shouldBe true
    }

    @Test
    fun read() {
        setOps.add(redisKey, "value1", "value2")

        val isMember = setOps.isMember(redisKey, "value1")
        val members = setOps.members(redisKey)

        isMember?.shouldBeTrue()
        setOf("value1", "value2").shouldBe(members)
    }

    @Test
    fun updateAndDelete() {
        setOps.add(redisKey, "oldValue")
        setOps.remove(redisKey, "oldValue")
        setOps.add(redisKey, "newValue")

        val members = setOps.members(redisKey)
        members?.contains("oldValue") shouldBe false
        members?.contains("newValue") shouldBe true
    }
}


