package jhkim105.tutorials.coroutines

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.lang.Thread.sleep
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis

class CoroutineTest {

    @Test
    @Disabled("Caused by: java.lang.OutOfMemoryError")
    fun testThread() {
        val threads = (1..100_000).map {
            thread {
                sleep(2000)
                print(".")
            }
        }
        threads.forEach { it.join() }
    }

    @Test
    fun runBlockingTest() {
        runBlocking {
            (1..100_000).map {
                launch {
                    delay(2000)
                    print(".")
                }
            }
        }

    }

    @Test
    fun runBlockingTest2() {
        println("start")
        val executionTime = measureTimeMillis {
            runBlocking {
                println("runBlocking start")
                launch {
                    delay(500)
                    println(Thread.currentThread().name)
                }
                launch {
                    delay(500)
                    println(Thread.currentThread().name)
                }
                println("runBlocking end")
            }
        }
        println("execution time: ${executionTime}ms")
    }

    @Test
    fun `runBlockingTest - single thread and blocking`() {
        val executionTime = measureTimeMillis {
            runBlocking {
                launch {
                    sleep(500)
                    println(Thread.currentThread().name)
                }
                launch {
                    sleep(500)
                    println(Thread.currentThread().name)
                }
            }
        }
        println("execution time: ${executionTime}ms")
    }

    @Test
    fun `runBlockingTest - multi thread and blocking`() {
        val executionTime = measureTimeMillis {
            runBlocking(Dispatchers.IO) {
                launch {
                    sleep(500)
                    println(Thread.currentThread().name)
                }
                launch {
                    sleep(500)
                    println(Thread.currentThread().name)
                }
            }
        }
        println("execution time: ${executionTime}ms")
    }
}

