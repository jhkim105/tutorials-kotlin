package jhkim105.dualcache.cache

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.aop.support.AopUtils
import org.springframework.cache.support.NullValue
import org.springframework.stereotype.Component

@Aspect
@Component
class LocalCacheAspect(
	private val localCacheRegistry: LocalCacheRegistry,
	private val cacheKeyGenerator: CacheKeyGenerator
) {

	@Around("@annotation(localCache)")
	fun applyLocalCache(joinPoint: ProceedingJoinPoint, localCache: LocalCache): Any? {
		val method = resolveMethod(joinPoint)
		val cacheName = if (localCache.cacheName.isNotBlank()) {
			localCache.cacheName
		} else {
			"${method.declaringClass.name}.${method.name}"
		}
		val cache = localCacheRegistry.resolve(
			LocalCacheSpec(cacheName, localCache.ttlSeconds, localCache.maximumSize)
		)
		val cacheKey = cacheKeyGenerator.generate(cacheName, joinPoint.target, method, joinPoint.args)
		val cachedValue = cache.getIfPresent(cacheKey)
		if (cachedValue != null) {
			return if (cachedValue is NullValue) null else cachedValue
		}
		val result = joinPoint.proceed()
		if (result != null) {
			cache.put(cacheKey, result)
		} else if (localCache.cacheNull) {
			cache.put(cacheKey, NullValue.INSTANCE)
		}
		return result
	}

	private fun resolveMethod(joinPoint: ProceedingJoinPoint) =
		AopUtils.getMostSpecificMethod(
			(joinPoint.signature as MethodSignature).method,
			joinPoint.target.javaClass
		)
}
