package jhkim105.tutorials.redis

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.SetOperations
import org.springframework.data.redis.core.ZSetOperations
import kotlin.test.Test

@SpringBootTest
class RedisSortedSetTest @Autowired constructor (
    private val redisTemplate: RedisTemplate<String, String>
) {

    private val key = "test:zset"

    @BeforeEach
    fun clearRedisSet() {
        redisTemplate.delete(key)
    }

    @Test
    fun sortedSetBasicTest() {
        val zSetOps: ZSetOperations<String, String> = redisTemplate.opsForZSet()

        zSetOps.add(key, "apple", 10.0)
        zSetOps.add(key, "banana", 5.0)
        zSetOps.add(key, "cherry", 20.0)

        // score asc
        val range = zSetOps.range(key, 0, -1)
        range shouldContainExactly  listOf("banana", "apple", "cherry")

        // score desc
        val reverseRange = zSetOps.reverseRange(key, 0, -1)
        reverseRange shouldContainExactly  listOf("cherry", "apple", "banana")

        // score between
        val rangeByScore = zSetOps.rangeByScore(key, 5.0, 15.0)
        rangeByScore shouldContainExactly listOf("banana", "apple")

        // update
        zSetOps.add(key, "apple", 30.0)

        // tuple(value, score)
        val rangeWithScores = zSetOps.rangeWithScores(key, 0, -1)
        rangeWithScores?.forEach { tuple -> println("${tuple.value}, ${tuple.score}") }
        rangeWithScores?.map {it.value} shouldContainExactly listOf("banana", "cherry", "apple")
        rangeWithScores?.find {it.value == "apple"}?.score shouldBe  30.0
    }

}