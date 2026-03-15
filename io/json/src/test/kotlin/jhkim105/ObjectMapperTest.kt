package jhkim105

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ObjectMapperTest {

    private val objectMapper = jacksonObjectMapper()

    @Test
    fun deserializeListTest() {
        val source = listOf(Item(1, "apple"), Item(2, "banana"))
        val json = objectMapper.writeValueAsString(source)

        val target = deserialize(json, object : TypeReference<List<Item>>() {})
        source shouldBe target
    }

    @Test
    fun deserializeObjectTest() {
        val source = Item(1, "apple")
        val json = objectMapper.writeValueAsString(source)

        val target = deserialize(json, object : TypeReference<Item>() {})
        source shouldBe target
    }

    fun <T> deserialize(json: String, type: TypeReference<T>): T {
        return objectMapper.readValue(json, type)
    }

    data class Item(val id: Int, val name: String)
}
