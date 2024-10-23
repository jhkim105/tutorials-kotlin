package jhkim105.tutorials.batch.adapter.out.persistence

import jhkim105.tutorials.batch.domain.model.Stock
import org.springframework.data.jpa.repository.JpaRepository

interface StockJpaRepository : JpaRepository<StockJpaEntity, String> {
}