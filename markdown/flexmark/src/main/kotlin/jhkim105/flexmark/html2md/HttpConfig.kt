package jhkim105.flexmark.html2md

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class HttpConfig {

    @Bean
    fun restClient(): RestClient =
        RestClient.builder()
            .defaultHeader("User-Agent", "MarkdownFetcher/1.0")
            .build()
}