package jhkim105.tutorials.jpadatetime

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

private val logger = KotlinLogging.logger {}

@SpringBootApplication
class JpaDateTimeApplication

fun main(args: Array<String>) {
    logger.info { "Starting JpaDateTimeApplication..." }
    runApplication<JpaDateTimeApplication>(*args)
}
