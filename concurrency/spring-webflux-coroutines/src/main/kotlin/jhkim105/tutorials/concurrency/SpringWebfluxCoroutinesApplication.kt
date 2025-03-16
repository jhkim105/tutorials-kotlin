package jhkim105.tutorials.concurrency

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SpringWebfluxCoroutinesApplication

fun main(args: Array<String>) {
    runApplication<SpringWebfluxCoroutinesApplication>(*args)
}
