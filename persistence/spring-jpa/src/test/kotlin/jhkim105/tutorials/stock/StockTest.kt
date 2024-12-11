package jhkim105.tutorials.stock

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class StockTest(
    @Autowired val stockJpaRepository: StockJpaRepository,
    @Autowired val stockService: StockService

) {

    @Autowired
    private lateinit var stockWarningJpaRepository: StockWarningJpaRepository
    var stockId: Long? = null

    @BeforeEach
    fun beforeEach() {
        val stock = stockJpaRepository.save(Stock(code = "stock01"))
        stockId = stock.id

        stockWarningJpaRepository.save(StockWarning(stock = stock, warningType = StockWarningType.MANGED_STOCK))
    }


    @Test
    @DisplayName("관계를 통한 저장 (Stock.stockWarnings)")
    fun stockWarningTest() {
        stockService.saveAll(stockId!!, setOf(StockWarningType.LIQUIDATION))
        val stockWarnings = stockService.getStockWarnings(stockId!!)
        assertThat(stockWarnings).hasSize(1)

        val stocks = stockService.findStocksByStockWarningType(StockWarningType.LIQUIDATION)
        assertThat(stocks).hasSize(1)
    }

}