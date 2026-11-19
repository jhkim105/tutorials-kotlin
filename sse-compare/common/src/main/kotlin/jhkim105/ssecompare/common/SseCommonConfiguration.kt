package jhkim105.ssecompare.common

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(SseProperties::class)
class SseCommonConfiguration {
    @Bean
    fun sseMetrics(meterRegistry: MeterRegistry): SseMetrics = SseMetrics(meterRegistry)
}
