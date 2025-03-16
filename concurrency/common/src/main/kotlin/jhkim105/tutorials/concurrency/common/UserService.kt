package jhkim105.tutorials.concurrency.common

import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

class UserService {
    private val log = LoggerFactory.getLogger(javaClass)

    fun getNowSomeSleep(sleepTime: Long): LocalDateTime {
        log.info("[${Thread.currentThread()}] Sleep for $sleepTime ms.")
        Thread.sleep(sleepTime)
        log.info("[${Thread.currentThread()}] Sleep done.")
        return LocalDateTime.now()
    }

    suspend fun getNowSomeDelay(delayTime: Long): LocalDateTime {
        log.info("[${Thread.currentThread()}] Delay for $delayTime ms.")
        delay(delayTime)
        log.info("[${Thread.currentThread()}] Delay done.")
        return LocalDateTime.now()
    }
}