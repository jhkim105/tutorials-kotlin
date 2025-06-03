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

    beforeTest {
        stockHistoryRepository.deleteAll()
        stockRepository.deleteAll()
    }

    "should create stock history on exchangeCode Or stockCode update" {
        // Given
        val stock = Stock(
            exchangeCode = "201",
            stockCode = "FE",
            stockName = "Stock A",
        )
        stockRepository.save(stock)

        // When
        stock.stockCode = "META"
        stockRepository.save(stock)

        // Then
        val stockHistories = stockHistoryRepository.findAll()
        val stockHistory = stockHistories.maxByOrNull { it.createdAt }!!
        stockHistories.size shouldBe 2
        stockHistory.exchangeCode shouldBe "201"
        stockHistory.stockCode shouldBe "META"
        stockHistory.createdAt.isBefore(LocalDateTime.now()) shouldBe true
    }

    "should not create stock history on stockName update" {
        // Given
        val stock = Stock(
            exchangeCode = "201",
            stockCode = "FE",
            stockName = "Stock A",
        )
        stockRepository.save(stock)

        // When
        stock.stockName = "Stock B"
        stockRepository.save(stock)

        // Then
        val stockHistories = stockHistoryRepository.findAll()
        stockHistories.size shouldBe 1
    }
})