package jhkim105.tutorials.coroutines

import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.system.measureTimeMillis

@Service
class UserService {
    private val log = LoggerFactory.getLogger(javaClass)

    fun doSomethingWithSleep(delay: Long): Long {
        return measureTimeMillis {
            log.info("Sleep for $delay ms. ${Thread.currentThread().name}")
            Thread.sleep(delay)
            log.info("done. ${Thread.currentThread().name}")
        }

    }

    suspend fun doSomethingWithDelay(delay: Long): Long {
        return measureTimeMillis {
            log.info("Delay for $delay ms. ${Thread.currentThread().name}" )
            delay(delay)
            log.info("done. ${Thread.currentThread().name}")
        }
    }
}