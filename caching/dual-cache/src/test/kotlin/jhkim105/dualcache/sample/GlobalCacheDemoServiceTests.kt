package jhkim105.dualcache.sample

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.ints.shouldBeGreaterThan

class GlobalCacheDemoServiceTests : StringSpec({

	val service = GlobalCacheDemoService()

	"loadSnapshot returns a global payload tied to the identifier" {
		val identifier = "alpha"

		val snapshot = service.loadSnapshot(identifier)

		snapshot.identifier shouldBe identifier
		snapshot.sequence shouldBeGreaterThan 0
	}

	"loadSnapshot produces new data on repeated calls" {
		val identifier = "alpha"

		val first = service.loadSnapshot(identifier)
		val second = service.loadSnapshot(identifier)

		second shouldNotBe first
	}

	"loadSnapshotList returns five distinct entries with the expected shape" {
		val identifier = "beta"

		val snapshots = service.loadSnapshotList(identifier)

		snapshots shouldHaveSize 5
		snapshots.distinct().size shouldBe snapshots.size
		snapshots.forEach {
			it.identifier shouldBe identifier
			it.sequence shouldBeGreaterThan 0
		}
	}
})
