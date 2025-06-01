package jhkim105.tutorials.jpa

import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class StockService(
    private val stockHistoryRepository: StockHistoryRepository,
    private val stockPriceRepository: StockPriceRepository
) {

    // 가격 정보를 조회하는 메소드
    fun findPriceByStockCodeAndExchangeCode(exchangeCode: String, stockCode: String, businessDate: LocalDate): List<StockPrice> {
        // StockHistory에서 가장 최신 이력을 조회
        val history = stockHistoryRepository
            .findTopByExchangeCodeAndStockCodeAndBusinessDateBeforeOrderByBusinessDateDesc(exchangeCode, stockCode, businessDate)

        // 해당 시점에 가장 최신의 이력이 없으면 빈 리스트 반환
        if (history != null) {
            // 해당 이력에 맞는 가격 정보 조회
            return stockPriceRepository.findByExchangeCodeAndStockCodeAndBusinessDateLessThanEqual(exchangeCode, stockCode, businessDate)
        }

        return emptyList() // 변경 이력이 없으면 빈 리스트 반환
    }
}