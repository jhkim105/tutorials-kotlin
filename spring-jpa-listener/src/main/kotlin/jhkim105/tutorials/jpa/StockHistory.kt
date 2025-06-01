package jhkim105.tutorials.jpa

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
class StockHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val stockId: Long,
    val exchangeCode: String,
    val stockCode: String,
    val businessDate: LocalDate,
    val createdAt: LocalDateTime
)

interface StockHistoryRepository : JpaRepository<StockHistory, Long> {

    fun findTopByExchangeCodeAndStockCodeAndBusinessDateBeforeOrderByBusinessDateDesc(
        exchangeCode: String,
        stockCode: String,
        businessDate: LocalDate
    ): StockHistory?
}