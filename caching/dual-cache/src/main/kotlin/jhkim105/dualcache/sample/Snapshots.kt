package jhkim105.dualcache.sample

import java.time.Instant

data class LocalSnapshot(
	val identifier: String,
	val sequence: Int,
	val issuedAt: Instant
)

data class GlobalSnapshot(
	val identifier: String,
	val sequence: Int,
	val issuedAt: Instant
)
