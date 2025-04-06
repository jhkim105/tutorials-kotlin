package jhkim105.tutorials.redis.config

import jhkim105.tutorials.redis.dlock.persistence.IdGenerator
import jhkim105.tutorials.redis.streams.RedisMessageConsumer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.stream.Consumer
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.connection.stream.ReadOffset
import org.springframework.data.redis.connection.stream.StreamOffset
import org.springframework.data.redis.stream.StreamMessageListenerContainer
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions
import java.time.Duration

const val DEMO_STREAM_KEY = "stream:demo"

@Configuration
class RedisStreamConfig {

    @Bean
    fun streamListenerContainer(
        connectionFactory: RedisConnectionFactory
    ): StreamMessageListenerContainer<String, MapRecord<String, String, String>> {
        val containerOptions = StreamMessageListenerContainerOptions
            .builder().pollTimeout(Duration.ofMillis(100)).build()

        val container = StreamMessageListenerContainer.create(
            connectionFactory,
            containerOptions
        )

        return container
    }

    @Bean
    fun startConsumer(
        container: StreamMessageListenerContainer<String, MapRecord<String, String, String>>,
        connectionFactory: RedisConnectionFactory,
        listener: RedisMessageConsumer
    ) :  StreamMessageListenerContainer<String, MapRecord<String, String, String>> {
        val groupName = "group1"
        val streamKey = DEMO_STREAM_KEY

        try {
            val commands = connectionFactory.connection.streamCommands()
            commands.xGroupCreate(streamKey.toByteArray(), groupName, ReadOffset.latest(), true)
        } catch (e: Exception) {
            println("Consumer group already exists or stream not initialized: ${e.message}")
        }

        container.receiveAutoAck(
            Consumer.from(groupName, "consumer-1"),
            StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
            listener
        )

        container.start()
        return container
    }
}