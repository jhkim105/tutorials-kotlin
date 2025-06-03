package jhkim105.tutorials.jpa.event

import jhkim105.tutorials.jpa.Stock
import jhkim105.tutorials.jpa.StockHistory
import jhkim105.tutorials.jpa.StockHistoryRepository
import org.hibernate.event.spi.*
import org.hibernate.persister.entity.EntityPersister
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime


@Component
class StockEventListener(
    private val stockHistoryRepository: StockHistoryRepository
) : PostInsertEventListener, PreUpdateEventListener, PostUpdateEventListener {

    override fun onPostUpdate(event: PostUpdateEvent) {
        val entity = event.entity
        if (entity !is Stock) return
        log.info("onPostUpdate")
        event.session.actionQueue.registerProcess { success, _ ->
            if (success) {
                log.info("onPostUpdate PostCommit success: [{}]", event.entity)
            }
        }
    }

    override fun onPostInsert(event: PostInsertEvent) {
        val entity = event.entity
        if (entity !is Stock) return
        log.info("onPostInsert")
        val stockHistory = StockHistory(
            stockId = entity.id,
            exchangeCode = entity.exchangeCode,
            stockCode = entity.stockCode,
            createdAt = LocalDateTime.now(),
            businessDate = LocalDateTime.now().toLocalDate()
        )
        stockHistoryRepository.save(stockHistory)

        event.session.actionQueue.registerProcess { success, _ ->
            if (success) {
                log.info("onPostInsert PostCommit success: [{}]", event.entity)
            }
        }
    }

    override fun onPreUpdate(event: PreUpdateEvent): Boolean {
        val entity = event.entity
        if (entity !is Stock) return false
        val props = event.persister.propertyNames
        val oldState = event.oldState
        val newState = event.state

        val changedFields = EntityChangeDetector.detectChangedFields(
            propertyNames = props,
            oldState = oldState,
            newState = newState
        )

        if ("stockCode" in changedFields || "exchangeCode" in changedFields) {
            stockHistoryRepository.save(
                StockHistory(
                    stockId = entity.id,
                    stockCode = entity.stockCode,
                    exchangeCode = entity.exchangeCode,
                    businessDate = LocalDate.now(),
                    createdAt = LocalDateTime.now()
                )
            )
        }

        return false
    }

    override fun requiresPostCommitHandling(entityPersister: EntityPersister): Boolean = false // true, false 동작 차이 없음


    companion object {
        private val log = LoggerFactory.getLogger(StockEventListener::class.java)
    }
}