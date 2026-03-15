package jhkim105.tutorials.kotlin.spring_web

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
//@ConfigurationPropertiesScan @WebMvcTests 시 에러
@EnableConfigurationProperties(BlogProperties::class)
class SpringKotlinApplication

fun main(args: Array<String>) {
  runApplication<SpringKotlinApplication>(*args)
}
