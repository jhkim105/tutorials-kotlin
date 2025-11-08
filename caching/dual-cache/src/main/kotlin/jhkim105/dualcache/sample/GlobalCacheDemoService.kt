package jhkim105.dualcache.sample

import jhkim105.dualcache.cache.GlobalCache
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger

@Service
class GlobalCacheDemoService {

	private val counter = AtomicInteger()

	@GlobalCache(cacheName = "globalDemo", ttlSeconds = 300)
	fun loadSnapshot(identifier: String): GlobalSnapshot {
		val sequence = counter.incrementAndGet()
		return GlobalSnapshot(
			identifier = identifier,
			sequence = sequence,
			issuedAt = Instant.now()
		)
	}

	@GlobalCache(cacheName = "globalDemoList", ttlSeconds = 180)
	fun loadSnapshotList(identifier: String): List<GlobalSnapshot> {
		val base = counter.incrementAndGet()
		return List(5) { idx ->
			GlobalSnapshot(
				identifier = identifier,
				sequence = base + idx,
				issuedAt = Instant.now()
			)
		}
	}
}
