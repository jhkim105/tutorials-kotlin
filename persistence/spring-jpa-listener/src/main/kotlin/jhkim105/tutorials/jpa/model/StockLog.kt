package jhkim105.tutorials.jpa.model

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "stock_log")
class StockLog(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val stockId: Long,

    @Column(nullable = false)
    val exchangeCode: String,

    @Column(nullable = false)
    val stockCode: String,

    val event: String,

    val commit: Boolean,

    @CreatedDate
    val createdAt: LocalDateTime = LocalDateTime.now(),
)

