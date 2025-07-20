package jhkim105.tutorials.kafka.config

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.util.backoff.FixedBackOff

@Configuration
class KafkaConfig {

    @Bean
    fun defaultErrorHandler(): DefaultErrorHandler {
        return DefaultErrorHandler(FixedBackOff(0L, 2))
    }

    @Bean
    fun topicSampleCompact(): NewTopic {
        return TopicBuilder.name(Topics.SAMPLE_COMPACT_TOPIC)
            .partitions(3)
            .replicas(1)
            .compact() // cleanup.policy=compact
            .config("min.cleanable.dirty.ratio", "0.01")
            .build()
    }

    object Topics {
        const val SAMPLE_TOPIC = "queue.sample"
        const val SAMPLE_COMPACT_TOPIC = "queue.sample-compact"
    }
}