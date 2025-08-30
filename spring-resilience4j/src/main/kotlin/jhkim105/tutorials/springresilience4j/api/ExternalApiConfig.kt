package jhkim105.tutorials.springresilience4j.api

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate


@Configuration
class ExternalApiConfig {
    @Bean
    @Qualifier("default")
    fun restTemplate(): RestTemplate? {
        return RestTemplateBuilder().rootUri("http://localhost:8080")
            .build()
    }
}