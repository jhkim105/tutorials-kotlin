package jhkim105.springkafkadynamic

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory

@Configuration
@EnableKafka
class KafkaConfig(
    private val consumerFactory: ConsumerFactory<String, String>
) {
    @Bean
    fun containerFactory(): ConcurrentKafkaListenerContainerFactory<String, String> =
        ConcurrentKafkaListenerContainerFactory<String, String>().apply {
            this.consumerFactory = this@KafkaConfig.consumerFactory
            setConcurrency(1)
            // 필요 시 에러핸들러, ackMode 등 추가
        }
}
