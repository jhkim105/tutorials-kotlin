package jhkim105.tutorials.mongodb.transaction.service

import jhkim105.tutorials.mongodb.transaction.domain.Order
import jhkim105.tutorials.mongodb.transaction.domain.Outbox
import jhkim105.tutorials.mongodb.transaction.repository.OrderRepository
import jhkim105.tutorials.mongodb.transaction.repository.OutboxRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderService(
        private val orderRepository: OrderRepository,
        private val outboxRepository: OutboxRepository
) {

    @Transactional
    fun createOrder(name: String, fail: Boolean = false) {
        val order = orderRepository.save(Order(name = name))

        outboxRepository.save(
                Outbox(
                        aggregateId = order.id,
                        aggregateType = "Order",
                        payload = "Order created: $name"
                )
        )

        if (fail) {
            throw RuntimeException("Intentional failure for rollback test")
        }
    }
}
