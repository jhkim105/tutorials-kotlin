package jhkim105.tutorials.kotlin

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DataClassTests {


    @Test
    fun test() {
        val sample1 = Sample("name 01")
        val sample2 = Sample("name 01")
        assertTrue(sample1 == sample2)
    }
}

data class Sample(
    private val name: String
)