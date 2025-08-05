package jhkim105.tutorials.springcachecaffeine

import com.github.benmanes.caffeine.cache.Caffeine
import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.cache.support.SimpleCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
@EnableCaching
class CacheConfig {

    private val log = LoggerFactory.getLogger(javaClass)

    @Bean
    fun cacheManager(): CacheManager {
        val manager = SimpleCacheManager()

        val caches = CacheConf.entries.map { cacheConf ->
            CaffeineCache(
                cacheConf.cacheName,
                Caffeine.newBuilder()
                    .recordStats()
                    .expireAfterAccess(cacheConf.ttl)
                    .expireAfterWrite(cacheConf.ttl)
                    .maximumSize(cacheConf.maxSize)
                    .removalListener<Any, Any> { key, value, cause ->
                        log.info("Removed. key: {}, value: {}, cause: {}", key, value, cause)
                    }
                    .build()
            )
        }

        manager.setCaches(caches)
        return manager
    }

    object CacheNames {
        const val CACHE_1 = "cache1"
        const val LOCAL_CACHE = "localCache"
    }

    private enum class CacheConf(
        val cacheName: String,
        val ttl: Duration,
        val maxSize: Long
    ) {
        CACHE_1_CONF(CacheNames.CACHE_1, Duration.ofSeconds(5), 1000),
        LOCAL_CACHE_CONF(CacheNames.LOCAL_CACHE, Duration.ofSeconds(10), 1000)
    }
}
