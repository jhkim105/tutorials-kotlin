package jhkim105.tutorials.redis

import org.redisson.client.codec.StringCodec
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SpringRedissonPubsubApplication

fun main(args: Array<String>) {
	runApplication<SpringRedissonPubsubApplication>(*args)
}
