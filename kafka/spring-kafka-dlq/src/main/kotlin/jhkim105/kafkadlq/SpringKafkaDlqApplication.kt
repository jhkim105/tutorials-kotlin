package jhkim105.kafkadlq

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SpringKafkaDlqApplication

fun main(args: Array<String>) {
    runApplication<SpringKafkaDlqApplication>(*args)
}
