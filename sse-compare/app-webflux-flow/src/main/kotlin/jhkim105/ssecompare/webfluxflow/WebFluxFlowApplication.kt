package jhkim105.ssecompare.webfluxflow

import jhkim105.ssecompare.common.SseCommonConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import

@SpringBootApplication(
    scanBasePackages = [
        "jhkim105.ssecompare.webfluxflow",
        "jhkim105.ssecompare.persistence.jpa",
        "jhkim105.ssecompare.persistence.r2dbc",
    ],
)
@Import(SseCommonConfiguration::class)
class WebFluxFlowApplication

fun main(args: Array<String>) {
    runApplication<WebFluxFlowApplication>(*args)
}
