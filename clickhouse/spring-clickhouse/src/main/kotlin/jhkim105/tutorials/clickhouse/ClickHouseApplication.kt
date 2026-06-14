package jhkim105.tutorials.clickhouse

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

private val logger = KotlinLogging.logger {}

@SpringBootApplication
class ClickHouseApplication

fun main(args: Array<String>) {
    runApplication<ClickHouseApplication>(*args)
    logger.info { "ClickHouse 스프링 부트 애플리케이션이 정상 작동 중입니다!" }
}
