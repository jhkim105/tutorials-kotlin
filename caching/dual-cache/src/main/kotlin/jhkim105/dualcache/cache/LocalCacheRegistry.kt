package jhkim105.dualcache.cache

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

data class LocalCacheSpec(
	val cacheName: String,
	val ttlSeconds: Long,
	val maximumSize: Long
) {
	init {
		require(cacheName.isNotBlank()) { "cacheName must not be blank" }
		require(ttlSeconds > 0) { "ttlSeconds must be positive" }
		require(maximumSize > 0) { "maximumSize must be positive" }
	}
}

@Component
class LocalCacheRegistry {
	private val caches = ConcurrentHashMap<String, CacheHolder>()

	fun resolve(spec: LocalCacheSpec): Cache<String, Any?> {
		return caches.compute(spec.cacheName) { _, holder ->
			if (holder == null || holder.spec != spec) {
				CacheHolder(spec, buildCache(spec))
			} else {
				holder
			}
		}!!.cache
	}

	private fun buildCache(spec: LocalCacheSpec): Cache<String, Any?> {
		return Caffeine.newBuilder()
			.expireAfterWrite(spec.ttlSeconds, TimeUnit.SECONDS)
			.maximumSize(spec.maximumSize)
			.recordStats()
			.build()
	}

	private data class CacheHolder(
		val spec: LocalCacheSpec,
		val cache: Cache<String, Any?>
	)
}
