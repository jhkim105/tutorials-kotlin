package jhkim105.dualcache.cache

import org.springframework.cache.interceptor.SimpleKeyGenerator
import org.springframework.stereotype.Component
import java.lang.reflect.Method

@Component
class CacheKeyGenerator {
	private val delegate = SimpleKeyGenerator()

	fun generate(
		providedCacheName: String,
		target: Any,
		method: Method,
		args: Array<Any?>
	): String {
		val cacheName = if (providedCacheName.isNotBlank()) {
			providedCacheName
		} else {
			"${target::class.java.name}.${method.name}"
		}
		if (args.isEmpty()) {
			return cacheName
		}
		val argsKey = delegate.generate(target, method, *args)
		return "$cacheName::$argsKey"
	}
}
