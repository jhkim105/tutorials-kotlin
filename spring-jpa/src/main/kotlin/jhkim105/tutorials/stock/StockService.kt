package jhkim105.tutorials.stock

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
class StockService(
    private val stockRepository: StockRepository,
    private val stockWarningRepository: StockWarningRepository
) {

    @Transactional
    fun saveAll(stockId: Long, stockWarningTypes: Set<StockWarningType>) {
        val stock = stockRepository.findByStockId(stockId)
            ?: throw IllegalArgumentException("Stock with id $stockId does not exist")

        stock.stockWarnings.clear()
        stockWarningTypes.forEach() { stock.stockWarnings.add(StockWarning(stock = stock, warningType = it)) }
        stockRepository.save(stock)
    }

    @Transactional(readOnly = true)
    fun getStockWarnings(stockId: Long): List<StockWarning> {
        return stockWarningRepository.findAll(stockId = stockId)
    }

    @Transactional(readOnly = true)
    fun findStocksByStockWarningType(stockWaringType: StockWarningType): List<Stock>? {
        return stockRepository.findStocksByStockWarningType(stockWaringType).map { stockWarning -> stockWarning.stock }
    }

}