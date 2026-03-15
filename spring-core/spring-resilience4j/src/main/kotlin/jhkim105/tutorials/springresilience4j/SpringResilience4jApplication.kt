package jhkim105.tutorials.springresilience4j

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.EnableAspectJAutoProxy

@SpringBootApplication
@EnableAspectJAutoProxy
class SpringResilience4jApplication

fun main(args: Array<String>) {
	runApplication<SpringResilience4jApplication>(*args)
}
