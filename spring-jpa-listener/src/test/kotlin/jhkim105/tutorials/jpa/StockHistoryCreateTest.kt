package jhkim105.tutorials.jpa

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime

@SpringBootTest
class StockHistoryCreateTest @Autowired constructor(
    val stockRepository: StockRepository,
    val stockHistoryRepository: StockHistoryRepository
) : StringSpec({

    "should create stock history on stock update" {
        // Given
        val stock = Stock(
            exchangeCode = "FE",
            stockCode = "201",
        )
        stockRepository.save(stock)

        // When
        stock.exchangeCode = "META"
        stockRepository.save(stock)

        // Then
        val stockHistory = stockHistoryRepository.findAll().maxByOrNull { it.createdAt }!!
        stockHistory.exchangeCode shouldBe "META"
        stockHistory.stockCode shouldBe "201"
        stockHistory.createdAt.isBefore(LocalDateTime.now()) shouldBe true
    }
})