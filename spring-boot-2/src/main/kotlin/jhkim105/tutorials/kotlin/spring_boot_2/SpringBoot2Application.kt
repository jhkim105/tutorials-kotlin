package jhkim105.tutorials.kotlin.spring_boot_2

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class SpringBoot2Application

fun main(args: Array<String>) {
    runApplication<SpringBoot2Application>(*args)
}
