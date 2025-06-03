package jhkim105.tutorials.jpa.event

import jhkim105.tutorials.jpa.Stock
import jhkim105.tutorials.jpa.event.StockEventListener.Companion
import org.hibernate.event.spi.PostCommitUpdateEventListener
import org.hibernate.event.spi.PostUpdateEvent
import org.hibernate.persister.entity.EntityPersister
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class StockCommitEventListener : PostCommitUpdateEventListener {
    override fun requiresPostCommitHandling(persister: EntityPersister): Boolean {
        return true // true 일 경우에만 onPostUpdate() 가 실행됨
    }

    override fun onPostUpdate(event: PostUpdateEvent) {
        val entity = event.entity
        if (entity !is Stock) return
        log.info("onPostUpdate")
    }

    override fun onPostUpdateCommitFailed(event: PostUpdateEvent) {
        val entity = event.entity
        if (entity !is Stock) return
        log.info("onPostUpdateCommitFailed")
    }


    companion object {
        private val log = LoggerFactory.getLogger(StockCommitEventListener::class.java)
    }
}