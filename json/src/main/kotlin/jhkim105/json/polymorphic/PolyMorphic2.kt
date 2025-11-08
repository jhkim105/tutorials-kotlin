package jhkim105.json.polymorphic

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

@JsonTypeInfo(
    use = JsonTypeInfo.Id.CLASS,
    include = JsonTypeInfo.As.PROPERTY,
    property = "@class"
)
sealed class Animal2 {
    abstract val name: String
}

data class Dog2(override val name: String) : Animal2()
data class Cat2(override val name: String) : Animal2()

fun main() {
    val mapper = jacksonObjectMapper()
    val animals: List<Animal2> = listOf(
        Dog2(name = "멍멍이"),
        Cat2(name= "야옹이")
    )

    // 직렬화
    val json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(animals)
    println("직렬화 결과:\n$json")

    // 역직렬화
    val deserialized = mapper.readValue(json, Array<Animal2>::class.java).toList()
    println("\n역직렬화 결과:")
    deserialized.forEach { println(it) }
}
