package jhkim105.authzdemo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AuthzDemoApiApplication

fun main(args: Array<String>) {
    runApplication<AuthzDemoApiApplication>(*args)
}
