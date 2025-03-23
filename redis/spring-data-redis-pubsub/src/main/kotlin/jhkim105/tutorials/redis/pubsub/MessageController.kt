package jhkim105.tutorials.redis.pubsub

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.util.*

@RestController
@RequestMapping(path = ["/messages"])
class MessageController(
    private val messagePublisher: RedisMessagePublisher
) {

    @GetMapping("/publish")
    fun publish() {
        val message = LocalDateTime.now().toString()
        messagePublisher.publish(message)
    }
}