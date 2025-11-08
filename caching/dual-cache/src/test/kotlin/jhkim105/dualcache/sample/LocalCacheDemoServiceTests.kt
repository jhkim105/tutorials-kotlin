package jhkim105.dualcache.sample

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.ints.shouldBeGreaterThan

class LocalCacheDemoServiceTests : StringSpec({

	val service = LocalCacheDemoService()

	"loadSnapshot returns a local payload tied to the identifier" {
		val identifier = "gamma"

		val snapshot = service.loadSnapshot(identifier)

		snapshot.identifier shouldBe identifier
		snapshot.sequence shouldBeGreaterThan 0
	}

	"loadSnapshot produces unique values without the cache layer" {
		val identifier = "gamma"

		val first = service.loadSnapshot(identifier)
		val second = service.loadSnapshot(identifier)

		second shouldNotBe first
	}

	"loadSnapshotList returns three distinct entries with the expected shape" {
		val identifier = "delta"

		val snapshots = service.loadSnapshotList(identifier)

		snapshots shouldHaveSize 3
		snapshots.distinct().size shouldBe snapshots.size
		snapshots.forEach {
			it.identifier shouldBe identifier
			it.sequence shouldBeGreaterThan 0
		}
	}
})
