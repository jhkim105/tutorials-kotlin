package jhkim105.authzsecurity

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AuthzSpringSecurityApiApplication

fun main(args: Array<String>) {
    runApplication<AuthzSpringSecurityApiApplication>(*args)
}
