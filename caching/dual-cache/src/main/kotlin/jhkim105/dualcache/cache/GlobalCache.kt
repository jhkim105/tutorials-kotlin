package jhkim105.dualcache.cache

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class GlobalCache(
	val cacheName: String = "",
	val ttlSeconds: Long = 120,
	val cacheNull: Boolean = false
)
