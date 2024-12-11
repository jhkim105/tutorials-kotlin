package jhkim105.tutorials.stock

import org.springframework.data.jpa.repository.JpaRepository

interface StockWarningJpaRepository: JpaRepository<StockWarning, Long> {
    fun findAllByStockId(stockId: Long): List<StockWarning>
    fun findAllByWarningType(warningType: StockWarningType): List<StockWarning>
}