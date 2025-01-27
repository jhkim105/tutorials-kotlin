package jhkim105.tutorials.stock

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class StockWarningTest(
    @Autowired val stockJpaRepository: StockJpaRepository,
    @Autowired val stockWarningService: StockWarningService

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
    fun stockWarningTest() {
        stockWarningService.saveAll(stockId!!, setOf(StockWarningType.LIQUIDATION, StockWarningType.INSUFFICIENT_DISCLOSURE))
        val stockWarnings = stockWarningService.getStockWarnings(stockId!!)
        assertThat(stockWarnings).hasSize(2)

        val stocks = stockWarningService.findStocksByStockWarningType(StockWarningType.LIQUIDATION)
        assertThat(stocks).hasSize(1)
    }

}