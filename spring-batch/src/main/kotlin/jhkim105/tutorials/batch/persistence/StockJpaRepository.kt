package jhkim105.tutorials.batch.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface StockJpaRepository : JpaRepository<StockJpaEntity, String> {
}