package jhkim105.tutorials.batch.persistence

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "stock")
class StockJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    val code: String,
    val price: Double,
    var createdAt: LocalDateTime,
) {


}