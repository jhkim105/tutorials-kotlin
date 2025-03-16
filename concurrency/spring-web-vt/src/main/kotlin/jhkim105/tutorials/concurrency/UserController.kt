package jhkim105.tutorials.concurrency

import jhkim105.tutorials.concurrency.common.UserService
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.system.measureTimeMillis

@RestController
@RequestMapping("/users")
class UserController {
    private val log = LoggerFactory.getLogger(this.javaClass)

    private val userService = UserService()


    @GetMapping("/blocking")
    @ResponseBody
    fun blocking(delay: Long): String {
        val executeTime = measureTimeMillis {
            userService.getNowSomeSleep(delay)
            userService.getNowSomeSleep(delay)
            userService.getNowSomeSleep(delay)
        }
        log.info("sleep: executeTime: $executeTime ms")
        return "executeTime: $executeTime ms"
    }

    @GetMapping("/nonblocking")
    @ResponseBody
    fun nonblocking(delay: Long): String {
        val executor: ExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2)

        val executeTime = measureTimeMillis {
            val future1 = CompletableFuture.supplyAsync({
                userService.getNowSomeSleep(delay)
            }, executor)
            val future2 = CompletableFuture.supplyAsync({
                userService.getNowSomeSleep(delay)
            }, executor)
            val future3 = CompletableFuture.supplyAsync({
                userService.getNowSomeSleep(delay)
            }, executor)
            future1.join()
            future2.join()
            future3.join()
        }

        log.info("delay: executeTime: $executeTime ms")
        return "executeTime: $executeTime ms"
    }

    @GetMapping("/nonblocking-coroutine")
    @ResponseBody
    fun nonblockingWithCoroutine(delay: Long): String {
        val dispatcher = Dispatchers.IO
//        val dispatcher = Executors.newFixedThreadPool(128).asCoroutineDispatcher()
        val executeTime = measureTimeMillis {
            runBlocking(dispatcher) {
                launch {
                    userService.getNowSomeSleep(delay)
                }
                launch {
                    userService.getNowSomeSleep(delay)
                }
                launch {
                    userService.getNowSomeSleep(delay)
                }
            }
        }

        log.info("delay: executeTime: $executeTime ms")
        return "executeTime: $executeTime ms"
    }

    @GetMapping("/nonblocking-coroutine-new-scope")
    @ResponseBody
    // 아래와 같이 쓰는 건 의미가 없음.
    fun nonblockingWithNewCoroutineScope(delay: Long): String {
        val dispatcher = Dispatchers.IO
        val executeTime = measureTimeMillis {
            runBlocking(dispatcher) {
                val job1 = CoroutineScope(dispatcher).launch {
                    userService.getNowSomeSleep(delay)
                }
                val job2 = CoroutineScope(dispatcher).launch {
                    userService.getNowSomeSleep(delay)
                }
                val job3 = CoroutineScope(dispatcher).launch {
                    userService.getNowSomeSleep(delay)
                }
                job1.join()
                job2.join()
                job3.join()
            }
        }

        log.info("delay: executeTime: $executeTime ms")
        return "executeTime: $executeTime ms"
    }

    @GetMapping("/nonblocking-coroutine-vt")
    @ResponseBody
    fun nonblockingWithCoroutineAndVirtualThread(delay: Long): String {
        val dispatcher = Executors.newVirtualThreadPerTaskExecutor().asCoroutineDispatcher()
        val executeTime = measureTimeMillis {
            runBlocking(dispatcher) {
                launch {
                    userService.getNowSomeSleep(delay)
                }
                launch {
                    userService.getNowSomeSleep(delay)
                }
                launch {
                    userService.getNowSomeSleep(delay)
                }
            }
        }

        log.info("delay: executeTime: $executeTime ms")
        return "executeTime: $executeTime ms"
    }
}
