package jhkim105.tutorials.kotlin

import org.springframework.boot.context.properties.ConfigurationProperties


@ConfigurationProperties(prefix = "service")
data class ServiceProperties(
    val name: String
)