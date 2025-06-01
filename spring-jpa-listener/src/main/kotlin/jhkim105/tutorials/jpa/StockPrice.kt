package jhkim105.tutorials.jpa

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import org.springframework.data.jpa.repository.JpaRepository
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
class StockPrice(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val exchangeCode: String,
    val stockCode: String,
    val price: BigDecimal,
    val businessDate: LocalDate
)

interface StockPriceRepository : JpaRepository<StockPrice, Long> {
    fun findByExchangeCodeAndStockCodeAndBusinessDateLessThanEqual(
        exchangeCode: String,
        stockCode: String,
        date: LocalDate
    ): List<StockPrice>
}