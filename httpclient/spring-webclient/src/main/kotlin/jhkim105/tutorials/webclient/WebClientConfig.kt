package jhkim105.tutorials.webclient

import io.netty.channel.ChannelOption
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import java.time.Duration

@Configuration
class WebClientConfig {
    @Bean
    fun webClient(): WebClient {
        val provider = ConnectionProvider.builder("custom")
            .metrics(true)
            .maxConnections(100)
            .pendingAcquireTimeout(Duration.ofSeconds(3))
            .maxIdleTime(Duration.ofSeconds(20))
            .maxLifeTime(Duration.ofMinutes(1))
            .build()

        val httpClient = HttpClient.create(provider)
            .responseTimeout(Duration.ofSeconds(30))
            .keepAlive(true)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)

        return WebClient.builder()
            .baseUrl("http://localhost:8888")
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .build()
    }

}