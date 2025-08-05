package jhkim105.tutorials.springcachecaffeine

import org.springframework.cache.CacheManager
import org.springframework.stereotype.Component

@Component
class LocalCacheAccessor(
    private val advice: CacheService
) {

    init {
        Companion.advice = advice
    }

    companion object {
        private lateinit var advice: CacheService

        fun <T> localCache(name: String, key: String, function: () -> T): T {
            return advice.cache(name, key, function)
        }
    }

    @Component
    class CacheService(
        private val cacheManager: CacheManager
    ) {
        fun <T> cache(name: String, key: String, function: () -> T): T {
            val cache = cacheManager.getCache(name)
            val cacheKey = "$name::$key"

            cache?.get(cacheKey)?.get()?.let {
                @Suppress("UNCHECKED_CAST")
                return it as T
            }

            val result = function.invoke()
            cache?.put(cacheKey, result)

            return result
        }

        fun <T> evict(name: String, key: String, function: () -> T): T {
            val cache = cacheManager.getCache(name)
            val cacheKey = "$name::$key"
            cache?.evict(cacheKey)
            return function.invoke()
        }
    }
}