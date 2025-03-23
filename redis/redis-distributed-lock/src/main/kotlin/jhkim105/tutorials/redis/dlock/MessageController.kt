package jhkim105.tutorials.redis.dlock

import jhkim105.tutorials.redis.dlock.messaging.RedisMessagePublisher
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping(path = ["/messages"])
class MessageController(
    private val messagePublisher: RedisMessagePublisher
) {

    @GetMapping("/publish")
    fun publish() {
        val message = Instant.now().toEpochMilli().toString()
        messagePublisher.publish(message)
    }
}