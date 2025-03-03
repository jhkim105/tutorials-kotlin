package jhkim105.tutorials.coroutines.blocking

import jhkim105.tutorials.coroutines.UserService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/blocking")
class BlockingController(
    private val userService: UserService
) {
    private val log = LoggerFactory.getLogger(this.javaClass)

    @GetMapping
    @ResponseBody
    fun doSomething(delay: Long): String {
        val executeTime = userService.doSomethingWithSleep(delay)
        log.info("executeTime: $executeTime ms")
        return "executeTime: $executeTime ms"
    }

}