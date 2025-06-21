package jhkim105.tutorials.jpa.model.listener

import jhkim105.tutorials.jpa.model.Stock
import jhkim105.tutorials.jpa.model.StockHistory
import jhkim105.tutorials.jpa.repository.StockHistoryRepository
import org.hibernate.event.spi.*
import org.hibernate.persister.entity.EntityPersister
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime


@Component
class StockEventListener(
    private val stockHistoryRepository: StockHistoryRepository
) : PostInsertEventListener, PostUpdateEventListener {

    override fun onPostInsert(event: PostInsertEvent) {
        val entity = event.entity
        if (entity !is Stock) return
        log.info("onPostInsert")
        val stockHistory = StockHistory(
            stockId = entity.id,
            changeDate = LocalDate.now(),
            beforeExchangeCode = null,
            beforeStockCode = null,
            afterExchangeCode = entity.exchangeCode,
            afterStockCode = entity.stockCode,
            createdAt = LocalDateTime.now()
        )
        stockHistoryRepository.save(stockHistory)
        event.session.actionQueue.registerProcess { success, _ ->
            if (success) {
                log.info("onPostInsert PostCommit success: [{}]", event.entity)
            }
        }
    }

    override fun onPostUpdate(event: PostUpdateEvent) {
        log.info("onPostUpdate")
        val entity = event.entity
        if (entity !is Stock) return
        val props = event.persister.propertyNames
        val oldState = event.oldState
        val newState = event.state

        val changedFields = JpaListenerUtils.detectChangedFields(
            propertyNames = props,
            oldState = oldState,
            newState = newState
        )

        if ("stockCode" in changedFields || "exchangeCode" in changedFields) {
            val beforeExchangeCode = JpaListenerUtils.getOldValue<String>("exchangeCode", props, oldState)
            val beforeStockCode = JpaListenerUtils.getOldValue<String>("stockCode", props, oldState)

            val stockHistory = StockHistory(
                stockId = entity.id,
                changeDate = LocalDate.now(),
                beforeExchangeCode = beforeExchangeCode,
                beforeStockCode = beforeStockCode,
                afterExchangeCode = entity.exchangeCode,
                afterStockCode = entity.stockCode,
                createdAt = LocalDateTime.now()
            )

            stockHistoryRepository.save(stockHistory)

        }
    }

    override fun requiresPostCommitHandling(entityPersister: EntityPersister): Boolean = false // true, false 동작 차이 없음

    companion object {
        private val log = LoggerFactory.getLogger(StockEventListener::class.java)
    }
}