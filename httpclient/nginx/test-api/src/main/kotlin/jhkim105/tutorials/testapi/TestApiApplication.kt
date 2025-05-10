package jhkim105.tutorials.testapi

import jakarta.servlet.http.HttpServletRequest
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.ConcurrentHashMap

@SpringBootApplication
class TestApiApplication

fun main(args: Array<String>) {
    runApplication<TestApiApplication>(*args)
}

@RestController
class DelayController {
    private val requested = ConcurrentHashMap<String, Boolean>()


    @GetMapping("/api/test")
    fun test(
        @RequestParam delay: Long = 0,
        request: HttpServletRequest
    ): String {
        println(">>> delaying $delay ms")
        Thread.sleep(delay)

        return "Response from server (delay: $delay ms)"
    }

    @GetMapping("/api/test2")
    fun test2(
        @RequestParam delay: Long = 0,
        request: HttpServletRequest
    ): String {
        val clientKey = request.remoteAddr
        val firstTime = requested.putIfAbsent(clientKey, true) == null

        if (firstTime && delay > 0) {
            println(">>> First request from $clientKey, delaying $delay ms")
            Thread.sleep(delay)
        } else {
            println(">>> Subsequent request from $clientKey, no delay")
        }

        return "Response from server (delay: ${if (firstTime) delay else 0} ms)"
    }


}