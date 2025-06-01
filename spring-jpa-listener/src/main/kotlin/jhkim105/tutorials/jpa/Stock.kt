package jhkim105.tutorials.jpa

import jakarta.persistence.*
import org.springframework.context.ApplicationContext
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Entity
@EntityListeners(StockEntityListener::class)
class Stock(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    var exchangeCode: String,
    var stockCode: String,

    @CreatedDate
    val createdAt: LocalDateTime? = null,

    @LastModifiedDate
    val updatedAt: LocalDateTime? = null
)

@Component
class StockEntityListener(
    private val applicationContext: ApplicationContext
) {

    private lateinit var stockHistoryRepository: StockHistoryRepository

    @PostPersist
    fun onPostPersist(stock: Stock) {
        if (!::stockHistoryRepository.isInitialized) {
            stockHistoryRepository = applicationContext.getBean(StockHistoryRepository::class.java)
        }

        val stockHistory = StockHistory(
            stockId = stock.id,
            exchangeCode = stock.exchangeCode,
            stockCode = stock.stockCode,
            createdAt = LocalDateTime.now(),
            businessDate = LocalDateTime.now().toLocalDate()
        )

        stockHistoryRepository.save(stockHistory)
    }

    @PostUpdate
    fun onPostUpdate(stock: Stock) {
        if (!::stockHistoryRepository.isInitialized) {
            stockHistoryRepository = applicationContext.getBean(StockHistoryRepository::class.java)
        }

        val stockHistory = StockHistory(
            stockId = stock.id,
            exchangeCode = stock.exchangeCode,
            stockCode = stock.stockCode,
            createdAt = LocalDateTime.now(),
            businessDate = LocalDateTime.now().toLocalDate()
        )

        stockHistoryRepository.save(stockHistory)
    }
}

interface StockRepository : JpaRepository<Stock, Long>