package jhkim105.tutorials.jpa.service

import jhkim105.tutorials.jpa.model.StockLog
import jhkim105.tutorials.jpa.model.listener.StockEvent
import jhkim105.tutorials.jpa.repository.StockLogRepository
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
class StockListener(
    private val stockLogRepository: StockLogRepository
) {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @EventListener
    fun handleStockLogEvent(event: StockEvent) {
        log.info("handle event. $event")
        val stockLog = StockLog(
            stockId = event.stockId,
            exchangeCode = event.exchangeCode,
            stockCode = event.stockCode,
            event = event.event,
            commit = event.commit
        )
        stockLogRepository.save(stockLog)
    }

    companion object {
        private val log = LoggerFactory.getLogger(StockListener::class.java)
    }
}