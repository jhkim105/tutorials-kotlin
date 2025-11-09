package jhkim105.dualcache.cache

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.aop.support.AopUtils
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Aspect
@Component
class GlobalCacheAspect(
	@Qualifier("cacheRedisTemplate")
	private val redisTemplate: RedisTemplate<String, String>,
	private val cacheKeyGenerator: CacheKeyGenerator,
	private val cacheValueConverter: CacheValueConverter
) {

	@Around("@annotation(globalCache)")
	fun applyGlobalCache(joinPoint: ProceedingJoinPoint, globalCache: GlobalCache): Any? {
		val method = resolveMethod(joinPoint)
		val cacheName = globalCache.cacheName.ifBlank {
            "${method.declaringClass.name}.${method.name}"
        }
		val cacheKey = cacheKeyGenerator.generate(cacheName, joinPoint.target, method, joinPoint.args)
		val returnType = method.genericReturnType
		val cachedPayload = redisTemplate.opsForValue().get(cacheKey)
		if (cachedPayload != null) {
			return cacheValueConverter.deserialize(cachedPayload, returnType)
		}
		val result = joinPoint.proceed()
		if (result != null) {
			val serialized = cacheValueConverter.serialize(result)
			redisTemplate.opsForValue().set(cacheKey, serialized, Duration.ofSeconds(globalCache.ttlSeconds))
		} else if (globalCache.cacheNull) {
			redisTemplate.opsForValue()
				.set(cacheKey, cacheValueConverter.serialize(null), Duration.ofSeconds(globalCache.ttlSeconds))
		}
		return result
	}

	private fun resolveMethod(joinPoint: ProceedingJoinPoint) =
		AopUtils.getMostSpecificMethod(
			(joinPoint.signature as MethodSignature).method,
			joinPoint.target.javaClass
		)
}
