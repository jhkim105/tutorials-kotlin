package jhkim105.tutorials.kotlin

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
@ConfigurationPropertiesScan
class SpringPropertiesApplication

fun main(args: Array<String>) {
    runApplication<SpringPropertiesApplication>(*args)
}
