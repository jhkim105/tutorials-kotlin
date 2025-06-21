package jhkim105.tutorials.jpa.model

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "stock_history")
class StockHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val stockId: Long,

    @Column(nullable = false)
    val changeDate: LocalDate,

    @Column(nullable = true)
    val beforeExchangeCode: String? = null,

    @Column(nullable = true)
    val beforeStockCode: String? = null,

    @Column(nullable = false)
    val afterExchangeCode: String,

    @Column(nullable = false)
    val afterStockCode: String,

    @CreatedDate
    val createdAt: LocalDateTime = LocalDateTime.now(),
)

