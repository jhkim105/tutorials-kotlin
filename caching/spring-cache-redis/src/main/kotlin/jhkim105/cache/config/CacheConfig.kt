package jhkim105.cache.config

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.CacheKeyPrefix
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import java.time.Duration

@Configuration
@EnableConfigurationProperties(CacheProperties::class)
class CacheConfig(
    private val objectMapper: ObjectMapper,
) {

    @Bean
    fun cacheManager(
        redisConnectionFactory: RedisConnectionFactory,
        cacheProperties: CacheProperties,
    ): CacheManager {
        val builder = RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(cacheConfiguration(cacheProperties.defaultTtl, cacheProperties.defaultPrefix))

        if (cacheProperties.caches.isNotEmpty()) {
            val perCache = cacheProperties.caches.mapValues { (cacheName, spec) ->
                cacheConfiguration(
                    ttl = spec.ttl ?: cacheProperties.defaultTtl,
                    prefix = spec.prefix ?: cacheProperties.defaultPrefix,
                    cacheName = cacheName,
                )
            }
            builder.withInitialCacheConfigurations(perCache)
        }

        return builder.build()
    }

    @Suppress("DEPRECATION")
    private fun cacheConfiguration(
        ttl: Duration,
        prefix: String,
        cacheName: String? = null,
    ): RedisCacheConfiguration {
        val redisObjectMapper = objectMapper.copy()
            .registerModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .activateDefaultTyping(
                LaissezFaireSubTypeValidator(),
                ObjectMapper.DefaultTyping.EVERYTHING,
                JsonTypeInfo.As.PROPERTY,
            )

        val serializer = GenericJackson2JsonRedisSerializer(redisObjectMapper)

        val computedPrefix = CacheKeyPrefix { name ->
            val nameToUse = cacheName ?: name
            if (prefix.isBlank()) {
                "$nameToUse::"
            } else {
                prefix + nameToUse + "::"
            }
        }

        return RedisCacheConfiguration
            .defaultCacheConfig()
            .entryTtl(ttl)
            .computePrefixWith(computedPrefix)
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
            .disableCachingNullValues()
    }
}
