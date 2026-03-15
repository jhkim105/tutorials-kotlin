package jhkim105.tutorials.jpa.repository

import jhkim105.tutorials.jpa.model.StockHistory
import org.springframework.data.jpa.repository.JpaRepository

interface StockHistoryRepository : JpaRepository<StockHistory, Long>