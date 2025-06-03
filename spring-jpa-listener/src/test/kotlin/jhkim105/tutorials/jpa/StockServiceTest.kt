package jhkim105.tutorials.jpa

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate

class StockServiceTest : StringSpec({

    val stockHistoryRepository = mockk<StockHistoryRepository>()
    val stockPriceRepository = mockk<StockPriceRepository>()

    val stockService = StockService(stockHistoryRepository, stockPriceRepository)

    "should return correct stock prices based on business date" {
        // given
        val exchangeCode = "META"
        val stockCode = "201"
        val businessDate = LocalDate.of(2023, 6, 5)

        val stockHistory = StockHistory(
            id = 1,
            stockId = 1,
            stockCode = "META",
            exchangeCode = "201",
            createdAt = LocalDate.of(2023, 6, 5).atStartOfDay(),
            businessDate = LocalDate.of(2023, 6, 5)
        )

        val stockPrices = listOf(
            StockPrice(
                id = 1,
                stockCode = "META",
                exchangeCode = "201",
                price = 105.0.toBigDecimal(),
                businessDate = LocalDate.of(2023, 6, 4)
            ),
            StockPrice(
                id = 2,
                stockCode = "META",
                exchangeCode = "201",
                price = 110.0.toBigDecimal(),
                businessDate = LocalDate.of(2023, 6, 5)
            )
        )

        // mocking
        every {
            stockHistoryRepository.findTopByExchangeCodeAndStockCodeAndBusinessDateBeforeOrderByBusinessDateDesc(
                exchangeCode, stockCode, businessDate
            )
        } returns stockHistory

        every {
            stockPriceRepository.findByExchangeCodeAndStockCodeAndBusinessDateLessThanEqual(
                exchangeCode, stockCode, businessDate
            )
        } returns stockPrices

        // when
        val result = stockService.findPriceByStockCodeAndExchangeCode(exchangeCode, stockCode, businessDate)

        // then
        result.size shouldBe 2
        result[0].price shouldBe 105.0.toBigDecimal()
        result[1].price shouldBe 110.0.toBigDecimal()
    }

    "should return empty list when no stock history is found" {
        // given
        val stockCode = "META"
        val exchangeCode = "201"
        val businessDate = LocalDate.of(2023, 6, 5)

        every {
            stockHistoryRepository.findTopByExchangeCodeAndStockCodeAndBusinessDateBeforeOrderByBusinessDateDesc(
                exchangeCode, stockCode, businessDate
            )
        } returns null

        // when
        val result = stockService.findPriceByStockCodeAndExchangeCode(
            exchangeCode = exchangeCode,
            stockCode = stockCode,
            businessDate = businessDate
        )

        // then
        result.shouldBeEmpty()
    }
})