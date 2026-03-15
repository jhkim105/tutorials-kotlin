package jhkim105.tutorials.batch.domain

import java.math.BigDecimal
import java.time.LocalDateTime

data class Stock(
    val id: String,
    var price: BigDecimal,
    var createdAt: LocalDateTime,
) {

}