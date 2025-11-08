package jhkim105.dualcache.cache

import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
@EnableCaching
class CacheConfiguration {

	@Bean("cacheRedisTemplate")
	fun cacheRedisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, String> {
		val template = RedisTemplate<String, String>()
		template.setConnectionFactory(connectionFactory)
		val stringSerializer = StringRedisSerializer()
		template.keySerializer = stringSerializer
		template.valueSerializer = stringSerializer
		template.hashKeySerializer = stringSerializer
		template.hashValueSerializer = stringSerializer
		template.afterPropertiesSet()
		return template
	}
}
