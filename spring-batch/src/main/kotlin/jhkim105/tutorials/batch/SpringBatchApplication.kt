package jhkim105.tutorials.batch

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import kotlin.system.exitProcess

@SpringBootApplication(scanBasePackages = ["jhkim105.tutorials.batch"])
class SpringBatchApplication

fun main(args: Array<String>) {
	val context = runApplication<SpringBatchApplication>(*args)
	val exitCode = SpringApplication.exit(context)
	exitProcess(exitCode)
}
