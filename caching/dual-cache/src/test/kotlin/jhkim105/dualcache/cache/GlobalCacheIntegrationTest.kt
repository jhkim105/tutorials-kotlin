package jhkim105.dualcache.cache

import jhkim105.dualcache.sample.GlobalCacheDemoService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.RedisTemplate
import java.lang.reflect.Type
import java.util.UUID
import java.util.concurrent.TimeUnit

@SpringBootTest
class GlobalCacheIntegrationTest @Autowired constructor(
	private val globalCacheDemoService: GlobalCacheDemoService,
	@Qualifier("cacheRedisTemplate")
	private val redisTemplate: RedisTemplate<String, String>,
	private val cacheKeyGenerator: CacheKeyGenerator,
	private val cacheValueConverter: CacheValueConverter
) {

	@BeforeEach
	fun flushRedis() {
		redisTemplate.execute<Unit> { connection ->
			connection.serverCommands().flushDb()
		}
	}

	@Test
	fun `loadSnapshot caches payload in redis and reuses it`() {
		val identifier = "global-${UUID.randomUUID()}"

		val first = globalCacheDemoService.loadSnapshot(identifier)
		val second = globalCacheDemoService.loadSnapshot(identifier)

		assertEquals(first, second, "Global cache should short-circuit duplicate lookups")

		val snapshotMethod = GlobalCacheDemoService::class.java.getDeclaredMethod("loadSnapshot", String::class.java)
		val cacheKey = cacheKeyGenerator.generate(
			"globalDemo",
			globalCacheDemoService,
			snapshotMethod,
			arrayOf(identifier)
		)
		val cachedPayload = redisTemplate.opsForValue().get(cacheKey)
		val cachedSnapshot = cachedPayload!!.deserialize(snapshotMethod.genericReturnType)
		assertEquals(first, cachedSnapshot, "Redis should hold the previously computed snapshot")

		val ttlSeconds = redisTemplate.getExpire(cacheKey, TimeUnit.SECONDS)
		assertTrue(ttlSeconds > 0, "TTL must be positive for active cache entries")
	}

	@Test
	fun `loadSnapshotList caches the entire list in redis`() {
		val identifier = "global-list-${UUID.randomUUID()}"

		val first = globalCacheDemoService.loadSnapshotList(identifier)
		val second = globalCacheDemoService.loadSnapshotList(identifier)

		assertEquals(first, second, "Global cache should reuse the cached list for repeated identifiers")

		val listMethod = GlobalCacheDemoService::class.java.getDeclaredMethod("loadSnapshotList", String::class.java)
		val cacheKey = cacheKeyGenerator.generate(
			"globalDemoList",
			globalCacheDemoService,
			listMethod,
			arrayOf(identifier)
		)
		val cachedPayload = redisTemplate.opsForValue().get(cacheKey)
		val cachedList = cachedPayload!!.deserialize(listMethod.genericReturnType)
		assertEquals(first, cachedList, "Redis should contain the serialized list payload")
	}

	private fun String.deserialize(returnType: Type): Any? {
		return cacheValueConverter.deserialize(this, returnType)
	}

}
