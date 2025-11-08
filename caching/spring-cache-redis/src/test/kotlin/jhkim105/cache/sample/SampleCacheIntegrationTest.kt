package jhkim105.cache.sample

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import jhkim105.cache.config.CacheProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SampleCacheIntegrationTest(
    @Autowired private val sampleService: SampleService,
    @Autowired private val redisTemplate: StringRedisTemplate,
    @Autowired private val redisConnectionFactory: RedisConnectionFactory,
    @Autowired private val cacheProperties: CacheProperties,
) {

    @BeforeAll
    fun ensureRedisRunning() {
        assumeTrue(isRedisReachable(), "Local Redis must be running on localhost:6379")
    }

    @BeforeEach
    fun clearState() {
        redisConnectionFactory.connection.use { connection ->
            connection.serverCommands().flushDb()
        }
        sampleService.resetCounters()
    }

    @Test
    fun `single sample calls hit the cache`() {
        val cacheKey = expectedKey(SampleService.SINGLE_CACHE, "42")

        val first = sampleService.getSample(42)
        val second = sampleService.getSample(42)

        assertThat(second).isEqualTo(first)
        assertThat(sampleService.singleInvocationCount()).isEqualTo(1)
        assertThat(redisTemplate.hasKey(cacheKey)).isTrue()
    }

    @Test
    fun `list responses reuse cached values`() {
        val cacheKey = expectedKey(SampleService.LIST_CACHE, "general:5")

        val first = sampleService.getSamples("general", 5)
        val second = sampleService.getSamples("general", 5)

        assertThat(second).isEqualTo(first)
        assertThat(sampleService.listInvocationCount()).isEqualTo(1)
        assertThat(redisTemplate.hasKey(cacheKey)).isTrue()
    }

    @Test
    fun `single cache entry uses configured ttl`() {
        val cacheKey = expectedKey(SampleService.SINGLE_CACHE, "99")

        sampleService.getSample(99)

        val ttlSeconds = redisTemplate.getExpire(cacheKey)
        assertThat(ttlSeconds).isPositive()
        assertThat(ttlSeconds).isLessThanOrEqualTo(cacheProperties.ttlFor(SampleService.SINGLE_CACHE).seconds)
    }

    private fun isRedisReachable(): Boolean =
        runCatching {
            redisConnectionFactory.connection.use { connection ->
                connection.ping()
            }
            true
        }.getOrDefault(false)

    private fun expectedKey(cacheName: String, key: String): String {
        val prefix = cacheProperties.prefixFor(cacheName)
        return buildString {
            if (prefix.isNotBlank()) {
                append(prefix)
            }
            append(cacheName)
            append("::")
            append(key)
        }
    }
}
