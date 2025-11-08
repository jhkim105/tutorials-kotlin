package jhkim105.cache.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "cache.redis")
data class CacheProperties(
    val defaultTtl: Duration = Duration.ofSeconds(60),
    val defaultPrefix: String = "",
    val caches: Map<String, CacheSpec> = emptyMap(),
) {

    data class CacheSpec(
        val ttl: Duration? = null,
        val prefix: String? = null,
    )

    fun ttlFor(cacheName: String): Duration =
        caches[cacheName]?.ttl ?: defaultTtl

    fun prefixFor(cacheName: String): String =
        caches[cacheName]?.prefix ?: defaultPrefix
}
