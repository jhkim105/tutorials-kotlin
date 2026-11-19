package jhkim105.ssecompare.webfluxflux

import jhkim105.ssecompare.common.SseCommonConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import

@SpringBootApplication(
    scanBasePackages = [
        "jhkim105.ssecompare.webfluxflux",
        "jhkim105.ssecompare.persistence.jpa",
        "jhkim105.ssecompare.persistence.r2dbc",
    ],
)
@Import(SseCommonConfiguration::class)
class WebFluxFluxApplication

fun main(args: Array<String>) {
    runApplication<WebFluxFluxApplication>(*args)
}
