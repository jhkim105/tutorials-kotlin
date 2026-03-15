package jhkim105.tutorials.jpa.model.listener

import jhkim105.tutorials.jpa.model.Stock
import jhkim105.tutorials.jpa.model.StockHistory
import jhkim105.tutorials.jpa.model.StockLog
import jhkim105.tutorials.jpa.repository.StockHistoryRepository
import jhkim105.tutorials.jpa.repository.StockLogRepository
import org.hibernate.event.spi.PostCommitInsertEventListener
import org.hibernate.event.spi.PostCommitUpdateEventListener
import org.hibernate.event.spi.PostInsertEvent
import org.hibernate.event.spi.PostUpdateEvent
import org.hibernate.persister.entity.EntityPersister
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component


@Component
class StockCommitEventListener(
    private val publisher: ApplicationEventPublisher
): PostCommitUpdateEventListener, PostCommitInsertEventListener {
    override fun requiresPostCommitHandling(persister: EntityPersister): Boolean {
        return true // true 일 경우에만 onPostXXX() 가 실행됨
    }

    override fun onPostInsert(event: PostInsertEvent) {
        val entity = event.entity
        if (entity !is Stock) return
        log.info("onPostInsert. $entity")
        publishLogEvent(entity, "INSERT", true)
    }

    private fun publishLogEvent(stock: Stock, event: String, commit: Boolean) {
        publisher.publishEvent(
            StockEvent(
                stockId = stock.id,
                exchangeCode = stock.exchangeCode,
                stockCode = stock.stockCode,
                event = event,
                commit = commit
            )
        )
    }

    override fun onPostInsertCommitFailed(event: PostInsertEvent) {
        val entity = event.entity
        if (entity !is Stock) return
        log.info("onPostUpdateCommitFailed. $entity")
        publishLogEvent(entity, "INSERT", false)

    }

    override fun onPostUpdate(event: PostUpdateEvent) {
        val entity = event.entity
        if (entity !is Stock) return
        log.info("onPostUpdate")
        publishLogEvent(entity, "UPDATE", true)
    }

    override fun onPostUpdateCommitFailed(event: PostUpdateEvent) {
        val entity = event.entity
        if (entity !is Stock) return
        log.info("onPostUpdateCommitFailed")
        publishLogEvent(entity, "UPDATE", false)
    }


    companion object {
        private val log = LoggerFactory.getLogger(StockCommitEventListener::class.java)
    }
}

data class StockEvent(
    val stockId: Long,
    val exchangeCode: String,
    val stockCode: String,
    val event: String,
    val commit: Boolean
)