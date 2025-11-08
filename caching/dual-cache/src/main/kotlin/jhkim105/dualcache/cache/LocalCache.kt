package jhkim105.dualcache.cache

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class LocalCache(
	val cacheName: String = "",
	val ttlSeconds: Long = 60,
	val maximumSize: Long = 1_000,
	val cacheNull: Boolean = false
)
