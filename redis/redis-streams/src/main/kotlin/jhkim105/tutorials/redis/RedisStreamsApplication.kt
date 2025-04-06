package jhkim105.tutorials.redis

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RedisStreamsApplication

fun main(args: Array<String>) {
	runApplication<RedisStreamsApplication>(*args)
}
