package jhkim105.tutorials.testcoverage

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class HelloServiceTest {

    private val service = HelloService()

    @Test
    fun testSayHello() {
        val result = service.sayHello("ChatGPT")
        assertEquals("Hello, ChatGPT", result)
    }

    @Test
    fun testAdd() {
        val result = service.add(2, 3)
        assertEquals(5, result)
    }
}
