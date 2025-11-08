package jhkim105.cache

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication
@EnableCaching
class SpringCacheRedisApplication

fun main(args: Array<String>) {
    runApplication<SpringCacheRedisApplication>(*args)
}
