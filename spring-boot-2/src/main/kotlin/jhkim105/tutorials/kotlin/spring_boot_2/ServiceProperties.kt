package jhkim105.tutorials.kotlin.spring_boot_2

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding


@ConfigurationProperties(prefix = "service")
@ConstructorBinding
data class ServiceProperties(
    val name: String
)