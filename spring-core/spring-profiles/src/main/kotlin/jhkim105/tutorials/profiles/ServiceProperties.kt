package jhkim105.tutorials.profiles

import org.springframework.boot.context.properties.ConfigurationProperties


@ConfigurationProperties(prefix = "service")
data class ServiceProperties(
    val name: String,
    val version: String
)