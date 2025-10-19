package jhkim105.springkafkadynamic

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.http.HttpStatus
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.listener.MessageListener
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.concurrent.ConcurrentHashMap

@Service
class DynamicKafkaListenerContainerService(
    private val containerFactory: ConcurrentKafkaListenerContainerFactory<String, String>
) {
    private val containers = ConcurrentHashMap<String, ConcurrentMessageListenerContainer<String, String>>()

    fun createAndStart(
        id: String,
        topics: List<String>,
        concurrency: Int = 1,
        listener: (ConsumerRecord<String, String>) -> Unit
    ) {
        require(topics.isNotEmpty()) { "topics must not be empty" }
        check(!containers.containsKey(id)) { "Container with id=$id already exists" }

        val props = ContainerProperties(*topics.toTypedArray()).apply {
            messageListener = MessageListener<String, String> { record -> listener(record) }
            pollTimeout = 1500L
            // 필요 시 ackMode, idleEventInterval 등 추가
        }

        val container = ConcurrentMessageListenerContainer(
            containerFactory.consumerFactory, props
        ).apply {
            setConcurrency(concurrency)
            isAutoStartup = false
        }

        containers[id] = container
        container.start()
    }

    fun stopAndRemove(id: String) {
        containers.remove(id)?.stop()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Dynamic container '$id' not found")
    }

    fun pause(id: String) = (containers[id] ?: notFound(id)).pause()
    fun resume(id: String) = (containers[id] ?: notFound(id)).resume()

    fun updateConcurrency(id: String, concurrency: Int) {
        val c = containers[id] ?: notFound(id)
        c.stop()
        c.concurrency = concurrency
        c.start()
    }

    private fun notFound(id: String): Nothing =
        throw ResponseStatusException(HttpStatus.NOT_FOUND, "Dynamic container '$id' not found")
}
