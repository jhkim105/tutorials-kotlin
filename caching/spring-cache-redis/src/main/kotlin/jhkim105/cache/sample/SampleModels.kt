package jhkim105.cache.sample

import java.time.Instant

data class SamplePayload(
    val id: Long,
    val category: String,
    val title: String,
    val description: String,
    val createdAt: Instant,
    val tags: List<String>,
)
