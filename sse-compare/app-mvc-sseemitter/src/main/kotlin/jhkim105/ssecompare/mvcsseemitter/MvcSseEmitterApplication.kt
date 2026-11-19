package jhkim105.ssecompare.mvcsseemitter

import jhkim105.ssecompare.common.SseCommonConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import

@SpringBootApplication(scanBasePackages = ["jhkim105.ssecompare.mvcsseemitter", "jhkim105.ssecompare.persistence.jpa"])
@Import(SseCommonConfiguration::class)
class MvcSseEmitterApplication

fun main(args: Array<String>) {
    runApplication<MvcSseEmitterApplication>(*args)
}
