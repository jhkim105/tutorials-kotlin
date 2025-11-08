package jhkim105.dualcache.sample

import jhkim105.dualcache.cache.LocalCache
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger

@Service
class LocalCacheDemoService {

	private val counter = AtomicInteger()

	@LocalCache(cacheName = "localDemo", ttlSeconds = 30, maximumSize = 100)
	fun loadSnapshot(identifier: String): LocalSnapshot {
		val sequence = counter.incrementAndGet()
		return LocalSnapshot(
			identifier = identifier,
			sequence = sequence,
			issuedAt = Instant.now()
		)
	}

	@LocalCache(cacheName = "localDemoList", ttlSeconds = 15, maximumSize = 50)
	fun loadSnapshotList(identifier: String): List<LocalSnapshot> {
		val base = counter.incrementAndGet()
		return List(3) { idx ->
			LocalSnapshot(
				identifier = identifier,
				sequence = base + idx,
				issuedAt = Instant.now()
			)
		}
	}
}
