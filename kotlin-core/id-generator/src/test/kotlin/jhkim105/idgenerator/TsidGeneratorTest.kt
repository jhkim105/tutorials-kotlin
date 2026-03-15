package jhkim105.idgenerator

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TsidGeneratorTest {

    @Test
    fun `generator should return String when String type is specified`() {
        val result: String = TsidGenerator.generator<String>()
        
        assertNotNull(result)
        assertTrue(result.isNotEmpty())
        println("[DEBUG_LOG] Generated String TSID: $result")
    }

    @Test
    fun `generator should return Long when Long type is specified`() {
        val result: Long = TsidGenerator.generator<Long>()
        
        assertNotNull(result)
        assertTrue(result > 0)
        println("[DEBUG_LOG] Generated Long TSID: $result")
    }

    @Test
    fun `generator should throw exception for unsupported type`() {
        val exception = assertThrows<IllegalArgumentException> {
            TsidGenerator.generator<Int>()
        }
        
        assertTrue(exception.message!!.contains("Unsupported type: Int"))
        assertTrue(exception.message!!.contains("Only String and Long are supported"))
        println("[DEBUG_LOG] Exception message: ${exception.message}")
    }

    @Test
    fun `generator should generate different values on multiple calls`() {
        val stringResult1: String = TsidGenerator.generator<String>()
        val stringResult2: String = TsidGenerator.generator<String>()
        val longResult1: Long = TsidGenerator.generator<Long>()
        val longResult2: Long = TsidGenerator.generator<Long>()
        
        assertTrue(stringResult1 != stringResult2)
        assertTrue(longResult1 != longResult2)
        println("[DEBUG_LOG] String results: $stringResult1, $stringResult2")
        println("[DEBUG_LOG] Long results: $longResult1, $longResult2")
    }
}