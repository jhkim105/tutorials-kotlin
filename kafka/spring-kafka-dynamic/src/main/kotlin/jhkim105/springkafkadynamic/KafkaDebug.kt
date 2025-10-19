package jhkim105.springkafkadynamic

import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationContext
import org.springframework.context.event.EventListener
import org.springframework.kafka.config.KafkaListenerEndpointRegistry
import org.springframework.stereotype.Component

// bean check
@Component
class Probe(ctx: ApplicationContext) {
    init {
        val beans = ctx.getBeansOfType(KafkaListenerEndpointRegistry::class.java)
        println("KafkaListenerEndpointRegistry beans = ${beans.keys}") // 비었으면 등록 안 된 것
    }
}

@Component
class KafkaListenerInspector(
    private val registry: KafkaListenerEndpointRegistry
) {
    @EventListener(ApplicationReadyEvent::class)
    fun printIds() {
        println("=== Registered Kafka listener IDs ===")
        registry.listenerContainerIds.forEach {
            println("- $it")
        }
    }
}