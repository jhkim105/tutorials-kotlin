package jhkim105.tutorials.stock

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class StockRepository(
    private val stockJpaRepository: StockJpaRepository,
    private val stockWarningJpaRepository: StockWarningJpaRepository
) {

    fun findByStockId(stockId: Long): Stock? {
        return stockJpaRepository.findByIdOrNull(stockId)
    }

    fun save(stock: Stock) {
        stockJpaRepository.save(stock)
    }


    fun findStocksByStockWarningType(stockWarningType: StockWarningType): List<StockWarning> {
        return stockWarningJpaRepository.findAllByWarningType(warningType = stockWarningType)
    }


}