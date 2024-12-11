package jhkim105.tutorials.stock

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
class StockWarningService(
    private val stockRepository: StockRepository,
    private val stockWarningRepository: StockWarningRepository
) {


    @Transactional
    fun saveAll(stockId: Long, stockWarningTypes: Set<StockWarningType>) {
        val stock = stockRepository.findByStockId(stockId)
            ?: throw IllegalArgumentException("Stock with id $stockId does not exist")

        val existsWarnings = stockWarningRepository.findAll(stockId)
        val warningsToDelete = existsWarnings.filter { it.warningType !in stockWarningTypes }
        stockWarningRepository.deleteAll(warningsToDelete)

        val existingWarningTypes = existsWarnings.map { it.warningType }.toSet()
        val warningsToAdd = stockWarningTypes.filter { it !in existingWarningTypes }.map { warningType ->
            StockWarning(
                stock = stock,
                warningType = warningType,
            )
        }
        stockWarningRepository.saveAl(warningsToAdd)
    }

    @Transactional(readOnly = true)
    fun getStockWarnings(stockId: Long): List<StockWarning> {
        return stockWarningRepository.findAll(stockId = stockId)
    }

    @Transactional(readOnly = true)
    fun findStocksByStockWarningType(stockWaringType: StockWarningType): List<Stock>? {
        return stockRepository.findStocksByStockWarningType(stockWaringType).map {stockWarning -> stockWarning.stock }
    }

}