package jhkim105.tutorials.batch.domain.model

import java.math.BigDecimal
import java.time.LocalDateTime

data class Stock(
    val id: String,
    var price: BigDecimal,
    var updatedAny: LocalDateTime,
) {

}