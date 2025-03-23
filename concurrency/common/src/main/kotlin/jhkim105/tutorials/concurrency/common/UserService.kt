package jhkim105.tutorials.concurrency.common

import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import java.time.LocalDateTime

class UserService {
    private val log = LoggerFactory.getLogger(javaClass)

    fun getNowSomeSleep(sleepTime: Long): LocalDateTime {
        log.info("[${Thread.currentThread()}][${MDC.get("caller")}] Sleep for $sleepTime ms.")
        Thread.sleep(sleepTime)
        log.info("[${Thread.currentThread()}][${MDC.get("caller")}] Sleep done.")
        return LocalDateTime.now()
    }

    suspend fun getNowSomeDelay(delayTime: Long): LocalDateTime {
        log.info("[${Thread.currentThread()}][${MDC.get("caller")}] Delay for $delayTime ms.")
        delay(delayTime)
        log.info("[${Thread.currentThread()}][${MDC.get("caller")}] Delay done.")
        return LocalDateTime.now()
    }
}