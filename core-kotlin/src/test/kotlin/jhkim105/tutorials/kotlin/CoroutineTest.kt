package jhkim105.tutorials.kotlin

import kotlinx.coroutines.*
import java.lang.Thread.sleep
import java.time.LocalDateTime
import java.util.concurrent.Executors
import kotlin.system.measureTimeMillis
import kotlin.test.Test

class CoroutineTest {


    private val sampleService = SampleService()

    @Test
    fun testDelay() {
        val time = measureTimeMillis {
            runBlocking {
                val job1 = launch {
                    sampleService.doDelay(2000)
                }

                val job2 = launch {
                    sampleService.doDelay(2000)
                }
            }
        }

        println("Test took $time ms")
    }

    @Test
    fun testSleep() {
        val time = measureTimeMillis {
            runBlocking {
                launch {
                    sampleService.doSleep(2000)
                }

                launch {
                    sampleService.doSleep(2000)
                }
            }

        }

        println("Test took $time ms")
    }

    @Test
    fun testSleepWithDispatcher() {
        val time = measureTimeMillis {
            runBlocking(Dispatchers.Default) {
                launch {
                    sampleService.doSleep(2000)
                }

                launch {
                    sampleService.doSleep(2000)
                }
            }
        }

        println("Test took $time ms")
    }

    @Test
    fun testSleepWithDispatcherToLaunch() {
        val time = measureTimeMillis {
            runBlocking {
                launch(Dispatchers.Default) {
                    sampleService.doSleep(2000)
                }

                launch(Dispatchers.Default) {
                    sampleService.doSleep(2000)
                }
            }
        }

        println("Test took $time ms")
    }

    @Test
    fun testSleepWithCustomDispatcher() {
        val customDispatcher = Executors.newFixedThreadPool(2).asCoroutineDispatcher()
        val time = measureTimeMillis {
            runBlocking(customDispatcher) {
                launch {
                    sampleService.doSleep(2000)
                }

                launch {
                    sampleService.doSleep(2000)
                }
            }
        }

        println("Test took $time ms")
    }

    @Test
    fun testSleepWithVT() {
        val customDispatcher = Executors.newVirtualThreadPerTaskExecutor().asCoroutineDispatcher()
        val time = measureTimeMillis {
            runBlocking(customDispatcher) {
                launch {
                    sampleService.doSleep(2000)
                }

                launch {
                    sampleService.doSleep(2000)
                }
            }
        }

        println("Test took $time ms")
    }

}

class SampleService {

    suspend fun doDelay(delayTime: Long): LocalDateTime {
        delay(delayTime)
        println("${Thread.currentThread()} done. delay $delayTime")
        return LocalDateTime.now()
    }

    fun doSleep(sleepTime: Long): LocalDateTime {
        sleep(sleepTime)
        println("${Thread.currentThread()} done. sleep $sleepTime")
        return LocalDateTime.now()
    }

}