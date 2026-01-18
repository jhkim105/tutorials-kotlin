package jhkim105.tutorials.mongodb.transaction.service

import io.kotest.matchers.shouldBe
import jhkim105.tutorials.mongodb.transaction.repository.OrderRepository
import jhkim105.tutorials.mongodb.transaction.repository.OutboxRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate

@SpringBootTest
class OrderServiceTest {

    @Autowired lateinit var orderService: OrderService

    @Autowired lateinit var orderRepository: OrderRepository

    @Autowired lateinit var outboxRepository: OutboxRepository

    @Autowired lateinit var mongoTemplate: MongoTemplate

    @BeforeEach
    fun setUp() {
        orderRepository.deleteAll()
        outboxRepository.deleteAll()
    }

    @Test
    fun `commit test`() {
        orderService.createOrder("test-order")

        orderRepository.count() shouldBe 1
        outboxRepository.count() shouldBe 1
    }

    @Test
    fun `rollback test`() {
        try {
            orderService.createOrder("test-order-fail", fail = true)
        } catch (e: Exception) {
            // expected
        }

        orderRepository.count() shouldBe 0
        outboxRepository.count() shouldBe 0
    }
}
