package jhkim105.json.polymorphic

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,        // 타입 이름 사용
    include = JsonTypeInfo.As.PROPERTY, // JSON 속성으로 포함
    property = "type"                   // "type" 필드로 구분
)
@JsonSubTypes(
    JsonSubTypes.Type(value = Dog1::class, name = "dog"),
    JsonSubTypes.Type(value = Cat1::class, name = "cat")
)
sealed class Animal1 {
    abstract val type: String
    abstract val name: String
}

data class Dog1(
    override val type: String = "dog",
    override val name: String,
    val barkVolume: Int
) : Animal1()

data class Cat1(
    override val type: String = "cat",
    override val name: String,
    val lives: Int
) : Animal1()

fun main() {
    val mapper = jacksonObjectMapper()
    val animals: List<Animal1> = listOf(
        Dog1(name = "멍멍이", barkVolume = 5),
        Cat1(name= "야옹이", lives = 9)
    )

    // 직렬화
    val json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(animals)
    println("직렬화 결과:\n$json")

    // 역직렬화
    val deserialized = mapper.readValue(json, Array<Animal1>::class.java).toList()
    println("\n역직렬화 결과:")
    deserialized.forEach { println(it) }
}