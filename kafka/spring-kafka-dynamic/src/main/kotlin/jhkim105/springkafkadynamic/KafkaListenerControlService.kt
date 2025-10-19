package jhkim105.springkafkadynamic

import org.springframework.beans.factory.ObjectProvider
import org.springframework.http.HttpStatus
import org.springframework.kafka.config.KafkaListenerEndpointRegistry
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class KafkaListenerControlService(
//    private val registry: KafkaListenerEndpointRegistry,
    private val registryProvider: ObjectProvider<KafkaListenerEndpointRegistry>
) {
    private val registry: KafkaListenerEndpointRegistry by lazy {
        registryProvider.getIfAvailable()
            ?: throw IllegalStateException("KafkaListenerEndpointRegistry not found. Did you enable @EnableKafka and scan KafkaConfig?")
    }

    fun start(id: String) =
        registry.getListenerContainer(id)?.start()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Listener '$id' not found")

    fun stop(id: String) =
        registry.getListenerContainer(id)?.stop()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Listener '$id' not found")

    fun pause(id: String) =
        registry.getListenerContainer(id)?.pause()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Listener '$id' not found")

    fun resume(id: String) =
        registry.getListenerContainer(id)?.resume()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Listener '$id' not found")

    fun updateConcurrency(id: String, concurrency: Int) {
        val container = registry.getListenerContainer(id)
            as? ConcurrentMessageListenerContainer<*, *>
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Listener '$id' not found or not concurrent")
        container.stop()                 // 안전하게 중지 후
        container.setConcurrency(concurrency)
        container.start()
    }
}
