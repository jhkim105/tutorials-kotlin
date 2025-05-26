package jhkim105.tutorials.kotlin.vt

import org.junit.jupiter.api.Test

class AsyncScopeTest {


    @Test
    fun test() {
        val results = awaitAll<String> {
            async { Thread.sleep(1000); "A" }
            async { Thread.sleep(1000); "B" }
            async { Thread.sleep(1000); "C" }
        }

        val unitResults = awaitAll<Unit> {
            asyncUnit { Thread.sleep(500); println("Done1") }
            asyncUnit { Thread.sleep(500); println("Done2") }
        }

        val timed = awaitAllWithTimeout<String>(1000) {
            async { Thread.sleep(500); "ok" }
            async { Thread.sleep(2000); "timeout" } // 취소됨
        }

        results.forEach { println(it) }
// Success("A"), Success("B"), Success("C")

        unitResults.forEach { println(it.isSuccess) }
// true, true

        timed.forEach { println(it.exceptionOrNull()) }
// null, java.util.concurrent.TimeoutException
    }
}