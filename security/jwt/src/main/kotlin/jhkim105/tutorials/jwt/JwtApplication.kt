package jhkim105.tutorials.jwt

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class JwtApplication

fun main(args: Array<String>) {
    runApplication<JwtApplication>(*args)
}
