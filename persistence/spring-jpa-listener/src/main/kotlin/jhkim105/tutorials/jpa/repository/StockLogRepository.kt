package jhkim105.tutorials.jpa.repository

import jhkim105.tutorials.jpa.model.StockLog
import org.springframework.data.jpa.repository.JpaRepository

interface StockLogRepository : JpaRepository<StockLog, Long>