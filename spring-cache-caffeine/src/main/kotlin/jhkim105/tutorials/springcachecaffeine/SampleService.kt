package jhkim105.tutorials.springcachecaffeine

import jhkim105.tutorials.springcachecaffeine.LocalCacheAccessor.Companion.localCache
import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@Service
class SampleService(
    private val cacheManager: CacheManager
) {

    private val log = LoggerFactory.getLogger(this::class.java)

    @Cacheable(CacheConfig.CacheNames.CACHE_1)
    fun getCache(key: String): String {
        log.info("getCache called")
        return getDate(key)
    }

    private fun getDate(pattern: String): String {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern))
    }

    @CacheEvict(value = [CacheConfig.CacheNames.CACHE_1], allEntries = true)
    fun evictAllCache() {
        // no-op
    }

    @CacheEvict(value = [CacheConfig.CacheNames.CACHE_1], key = "#cacheKey")
    fun evictCache(cacheKey: String) = {}

    @CachePut(value = [CacheConfig.CacheNames.CACHE_1])
    fun putCache(pattern: String): String {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern))
    }

    fun evictAllCacheByCacheManager() {
        cacheManager.getCache(CacheConfig.CacheNames.CACHE_1)!!.clear()
    }

    fun evictSingleCacheByCacheManager(cacheKey: String) {
        cacheManager.getCache(CacheConfig.CacheNames.CACHE_1)!!.evict(cacheKey)
    }

    fun getCachedByCall(pattern: String) {
        return localCache(CacheConfig.CacheNames.CACHE_1, pattern) { getDate(pattern) }

    }
}