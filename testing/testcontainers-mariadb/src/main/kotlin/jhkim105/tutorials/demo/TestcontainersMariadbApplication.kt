package jhkim105.tutorials.demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TestcontainersMariadbApplication

fun main(args: Array<String>) {
    runApplication<TestcontainersMariadbApplication>(*args)
}
