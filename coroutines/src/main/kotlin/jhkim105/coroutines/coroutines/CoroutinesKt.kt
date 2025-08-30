/**
 * https://www.youtube.com/watch?v=Wpco6IK1hmY&list=PLmtsMNDRU0BxRw895WX1rh5iOTNSYpSxh
 */
package jhkim105.coroutines.coroutines

import kotlinx.coroutines.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors

private val log: Logger = LoggerFactory.getLogger("coroutines")


// -Dkotlinx.coroutines.debug
// since 1.3+
suspend fun main(args: Array<String>) {
    demonContextInheritance()
}

//fun main(args: Array<String>) = runBlocking {
//    demonContextInheritance()
//}

suspend fun boilingWater() {
    log.info("Boiling water")
    delay(1000L)
    log.info("Water boiled")
}

suspend fun bathTime() {
    log.info("Going to the bathroom")
    delay(2000)
    log.info("Bath done, exiting")
}

suspend fun makeCoffee() {
    log.info("Making coffee")
    delay(500)
    log.info("Coffee done")
}


suspend fun sequentialMorningRoutine() {
    coroutineScope {
        bathTime()
    }

    coroutineScope {
        boilingWater()
    }
}

suspend fun concurrencyMorningRoutine() {
    coroutineScope {
        launch { bathTime() }
        launch { boilingWater() }
    }
}

suspend fun noStructConcurrencyMorningRoutine() {
    GlobalScope.launch { bathTime() }
    GlobalScope.launch { boilingWater() }
}


suspend fun morningRoutineWithCoffee() {
    coroutineScope {
        val bathTimeJob = launch { bathTime() }
        val boilingWaterJob = launch { boilingWater() }
        bathTimeJob.join()
        boilingWaterJob.join()
        launch { makeCoffee() }
    }
}

suspend fun morningRoutineWithCoffeeStructured() {
    coroutineScope {
        coroutineScope {
            launch { bathTime() }
            launch { boilingWater() }
        }
        launch { makeCoffee() }
    }
}

suspend fun preparingJavaCoffee(): String {
    log.info("Making coffee")
    delay(500)
    log.info("Coffee done")
    return "Java coffee"
}

suspend fun toastingBread(): String {
    log.info("Tasting bread")
    delay(500)
    log.info("toasted bread")
    return "Tasted bread"
}

suspend fun prepareBreakfast() {
    coroutineScope {
        val coffee = async { preparingJavaCoffee() }
        val toast = async { toastingBread() }
        val finalCoffee = coffee.await()
        val finalToast = toast.await()
        log.info("I'm eating $finalCoffee and drinking $finalToast")
    }
}

// cooperative scheduling - coroutines yield manually
suspend fun workingHard() {
    log.info("Working")
    while (true) {

    }
    delay(100)
    log.info("Work done")
}

suspend fun workingNicely() {
    log.info("Working")
    while (true) {
        delay(100)
    }
    log.info("Work done")
}

suspend fun breakTime() {
    log.info("Taking a break")
    delay(1000)
    log.info("Break done")
}

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun workHardRoutine() {
    val dispatcher: CoroutineDispatcher = Dispatchers.Default.limitedParallelism(1)
    coroutineScope {
        launch(dispatcher) { workingHard() }
        launch(dispatcher) { breakTime() }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun workHardNicelyRoutine() {
    val dispatcher: CoroutineDispatcher = Dispatchers.Default.limitedParallelism(1)
    coroutineScope {
        launch(dispatcher) { workingNicely() }
        launch(dispatcher) { breakTime() }
    }
}

val simpleDispatcher: CoroutineDispatcher = Dispatchers.Default
val blockingDispatcher: CoroutineDispatcher = Dispatchers.IO
val customDispatcher: CoroutineDispatcher = Executors.newFixedThreadPool(8).asCoroutineDispatcher()

// cancellation
suspend fun forgettingFriendBirthdayRoutine() {
    coroutineScope {
        val workingJob = launch { workingNicely() }
        launch {
            delay(3000)
            workingJob.cancelAndJoin()
            log.info("I forgot my friend's birthday! Buying a present now!")
        }
    }
}

suspend fun forgettingFriendBirthdayRoutineUncancelable() {
    coroutineScope {
        val workingJob = launch { workingHard() }
        launch {
            delay(3000)
            workingJob.cancelAndJoin()
            log.info("I forgot my friend's birthday! Buying a present now!")
        }
    }
}

class Desk : AutoCloseable {
    init {
        log.info("Starting to work on this desk")
    }

    override fun close() {
        log.info("Cleaning up the desk")
    }
}

// cancellation
suspend fun forgettingFriendBirthdayRoutineWithResource() {
    val desk = Desk()
    coroutineScope {
        val workingJob = launch {
            desk.use { _ ->
                workingNicely()
            }
        }

        workingJob.invokeOnCompletion { exception: Throwable? ->
            log.info("Make sure I talk to my colleagues that I'll be out for 30 minutes")
        }

        launch {
            delay(2000)
            workingJob.cancelAndJoin()
            log.info("I forgot my friend's birthday! Buying a present now!")
        }
    }
}

// cancellation propagates to child coroutine
suspend fun drinkWater() {
    while (true) {
        log.info("Drinking water")
        delay(1000)
    }
}

// cancellation
suspend fun forgettingFriendBirthdayRoutineStayHydrated() {
    coroutineScope {
        val workingJob = launch {
            launch { workingNicely() }
            launch { drinkWater() }
        }
        launch {
            delay(3000)
            workingJob.cancelAndJoin()
            log.info("I forgot my friend's birthday! Buying a present now!")
        }
    }
}

// coroutine context
suspend fun asynchronousGreeting() {
    coroutineScope {
        launch(CoroutineName("Greeting Coroutine") + Dispatchers.Default) {
            log.info("Hello. everyone")
        }
    }
}

suspend fun demonContextInheritance() {
    coroutineScope {
        launch(CoroutineName("Greeting Coroutine")) {
            log.info("[parent] Hello. everyone")
            launch {
                log.info("[child1] Hi there!")
            }
            launch(CoroutineName("Child Greeting Coroutine")) {
                log.info("[child2] Hi there!")
            }
        }
        delay(200)
        log.info("[parent] Hi again")
    }
}
