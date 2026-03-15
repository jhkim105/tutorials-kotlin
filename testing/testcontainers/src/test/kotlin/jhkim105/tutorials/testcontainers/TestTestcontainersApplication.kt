package jhkim105.tutorials.testcontainers

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
    fromApplication<TestcontainersApplication>().with(TestcontainersConfiguration::class).run(*args)
}
