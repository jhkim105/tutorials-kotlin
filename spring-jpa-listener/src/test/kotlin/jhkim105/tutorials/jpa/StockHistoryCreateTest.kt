package jhkim105.tutorials.jpa

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import jhkim105.tutorials.jpa.model.Stock
import jhkim105.tutorials.jpa.repository.StockHistoryRepository
import jhkim105.tutorials.jpa.repository.StockRepository
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

    "stockHistory 생성" {
        // given, when
        val stock = Stock(
            exchangeCode = "201",
            stockCode = "FE",
        )
        stockRepository.save(stock)

        stock.stockCode = "META"
        stockRepository.save(stock)

        // then
        val stockHistories = stockHistoryRepository.findAll()
        val stockHistory = stockHistories.maxByOrNull { it.createdAt }!!
        stockHistories.size shouldBe 2
        stockHistory.beforeExchangeCode shouldBe "201"
        stockHistory.afterExchangeCode shouldBe "201"
        stockHistory.beforeStockCode shouldBe "FE"
        stockHistory.afterStockCode shouldBe "META"
        stockHistory.createdAt.isBefore(LocalDateTime.now()) shouldBe true
    }

})