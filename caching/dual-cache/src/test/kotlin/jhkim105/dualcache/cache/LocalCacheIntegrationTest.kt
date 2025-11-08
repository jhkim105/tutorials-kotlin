package jhkim105.dualcache.cache

import jhkim105.dualcache.sample.LocalCacheDemoService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.UUID

@SpringBootTest
class LocalCacheIntegrationTest @Autowired constructor(
	private val localCacheDemoService: LocalCacheDemoService
) {

	@Test
	fun `loadSnapshot returns cached payload on subsequent lookups`() {
		val identifier = "local-${UUID.randomUUID()}"

		val first = localCacheDemoService.loadSnapshot(identifier)
		val second = localCacheDemoService.loadSnapshot(identifier)

		assertEquals(first, second, "Local cache should serve identical snapshot for repeated requests")
	}

	@Test
	fun `loadSnapshotList returns the same list instance while cached`() {
		val identifier = "local-list-${UUID.randomUUID()}"

		val first = localCacheDemoService.loadSnapshotList(identifier)
		val second = localCacheDemoService.loadSnapshotList(identifier)

		assertEquals(first, second, "Cached list entries should remain stable for the TTL window")
		assertSame(first, second, "Local cache should reuse the cached list instance")
	}

}
