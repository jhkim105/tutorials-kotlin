package jhkim105.tutorials.clickhouse.domain

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class ProductViewEvent(
    val id: UUID = UUID.randomUUID(),
    val productId: String,
    val userId: String,
    val price: BigDecimal,
    val urlPath: String,
    val referrer: String,
    val createdAt: LocalDateTime? = null
)
