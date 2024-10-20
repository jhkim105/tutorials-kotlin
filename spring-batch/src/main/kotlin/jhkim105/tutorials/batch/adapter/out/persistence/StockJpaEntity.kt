package jhkim105.tutorials.batch.adapter.out.persistence

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.UuidGenerator
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "stock")
class StockJpaEntity(
    @Id
    @UuidGenerator
    var id: String? = null,
    var price: BigDecimal,
    var updatedAt: LocalDateTime,
) {


}