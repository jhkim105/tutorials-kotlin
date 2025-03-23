package jhkim105.tutorials.kotlin

import java.time.Instant
import kotlin.test.Test

class DateTest {

    @Test
    fun testInstant() {
        println(Instant.now().toEpochMilli())
        println(Instant.now().nano)
        println(Instant.now().toEpochMilli().toString())
    }
}