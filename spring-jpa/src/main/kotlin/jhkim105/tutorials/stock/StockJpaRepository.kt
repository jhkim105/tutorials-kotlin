package jhkim105.tutorials.stock

import org.springframework.data.jpa.repository.JpaRepository

interface StockJpaRepository: JpaRepository<Stock, Long> {

}