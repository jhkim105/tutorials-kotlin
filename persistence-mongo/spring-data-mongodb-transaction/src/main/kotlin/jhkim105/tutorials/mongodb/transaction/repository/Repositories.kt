package jhkim105.tutorials.mongodb.transaction.repository

import jhkim105.tutorials.mongodb.transaction.domain.Order
import jhkim105.tutorials.mongodb.transaction.domain.Outbox
import org.springframework.data.mongodb.repository.MongoRepository

interface OrderRepository : MongoRepository<Order, String>
interface OutboxRepository : MongoRepository<Outbox, String>
