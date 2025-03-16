package jhkim105.tutorials.concurrency

import jhkim105.tutorials.concurrency.common.UserService
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import kotlin.system.measureTimeMillis

@RestController
@RequestMapping("/users")
class UserController {
    private val log = LoggerFactory.getLogger(this.javaClass)

    private val userService = UserService()

    @GetMapping("/nonblocking")
    @ResponseBody
    suspend fun doSomethingWithDelay(delay: Long): String {
        val executeTime = measureTimeMillis {
            coroutineScope {
                launch { userService.getNowSomeDelay(delay) }
                launch { userService.getNowSomeDelay(delay) }
                launch { userService.getNowSomeDelay(delay) }
            }
        }
        log.info("delay: executeTime: $executeTime ms")
        return "executeTime: $executeTime ms"
    }

    @GetMapping("/blocking")
    @ResponseBody
    fun doSomethingWithSleep(delay: Long): String {
        val executeTime = measureTimeMillis {
            userService.getNowSomeSleep(delay)
            userService.getNowSomeSleep(delay)
            userService.getNowSomeSleep(delay)
        }
        log.info("sleep: executeTime: $executeTime ms")
        return "executeTime: $executeTime ms"
    }

}