package jhkim105.tutorials.redis

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SpringDataRedisReactiveApplication

fun main(args: Array<String>) {
    runApplication<SpringDataRedisReactiveApplication>(*args)
}
