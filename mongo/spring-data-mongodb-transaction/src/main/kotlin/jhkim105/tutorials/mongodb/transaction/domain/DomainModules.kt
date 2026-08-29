package jhkim105.tutorials.mongodb.transaction.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.UUID

@Document(collection = "orders")
data class Order(
    @Id
    val id: String = UUID.randomUUID().toString(),
    val name: String
)

@Document(collection = "outbox")
data class Outbox(
    @Id
    val id: String = UUID.randomUUID().toString(),
    val aggregateId: String,
    val aggregateType: String,
    val payload: String
)
