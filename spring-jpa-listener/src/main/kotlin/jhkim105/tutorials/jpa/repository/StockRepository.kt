package jhkim105.tutorials.jpa.repository

import jhkim105.tutorials.jpa.model.Stock
import org.springframework.data.jpa.repository.JpaRepository

interface StockRepository : JpaRepository<Stock, Long>