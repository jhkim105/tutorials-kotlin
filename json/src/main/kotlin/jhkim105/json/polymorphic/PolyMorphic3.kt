package jhkim105.json.polymorphic

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

@JsonTypeInfo(
    use = JsonTypeInfo.Id.DEDUCTION // 고유 필드 조합으로 타입 추론, 필드가 동일한 타입이 있으면 안됨
)
sealed class Animal3 {
    abstract val name: String
}

data class Dog3(override val name: String, val barkVolume: Int) : Animal3()
data class Cat3(override val name: String, val age: Int) : Animal3()

fun main() {
    val mapper = jacksonObjectMapper()
    val animals: List<Animal3> = listOf(
        Dog3(name = "멍멍이", barkVolume = 5),
        Cat3(name= "야옹이", age = 9)
    )

    // 직렬화
    val json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(animals)
    println("직렬화 결과:\n$json")

    // 역직렬화
    val deserialized = mapper.readValue(json, Array<Animal3>::class.java).toList()
    println("\n역직렬화 결과:")
    deserialized.forEach { println(it) }
}
