package jhkim105.tutorials.stock

import org.springframework.stereotype.Repository

@Repository
class StockWarningRepository(
    private val stockWarningJpaRepository: StockWarningJpaRepository
) {
    fun findAll(stockId: Long): List<StockWarning> {
        return stockWarningJpaRepository.findAllByStockId(stockId = stockId)
    }

    fun deleteAll(stockWarnings: List<StockWarning>) {
        stockWarningJpaRepository.deleteAll(stockWarnings)
    }

    fun saveAl(warningsToAdd: List<StockWarning>) {
        stockWarningJpaRepository.saveAll(warningsToAdd)
    }

    fun findAll(stockWarningType: StockWarningType): List<StockWarning> {
        return stockWarningJpaRepository.findAllByWarningType(warningType = stockWarningType)
    }


}