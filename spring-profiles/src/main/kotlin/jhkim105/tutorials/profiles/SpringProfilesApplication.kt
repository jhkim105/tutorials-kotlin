package jhkim105.tutorials.profiles

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class SpringProfilesApplication

fun main(args: Array<String>) {
    runApplication<SpringProfilesApplication>(*args)
}
