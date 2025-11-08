package jhkim105.json.polymorphic

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = Dog::class, name = "dog"),
    JsonSubTypes.Type(value = Cat::class, name = "cat")
)
sealed class Animal { abstract val name: String }

data class Dog(
    override val name: String,
    val barkVolume: Int
) : Animal()

data class Cat(
    override val name: String,
    val lives: Int
) : Animal()

fun main() {
    val mapper = jacksonObjectMapper()

    val animals: List<Animal> = listOf(
        Dog("멍멍이", 5),
        Cat("야옹이", 9)
    )

    val json = mapper.writerFor(object : com.fasterxml.jackson.core.type.TypeReference<List<Animal>>() {})
        .withDefaultPrettyPrinter()
        .writeValueAsString(animals)
    println(json)

    val back: List<Animal> = mapper.readValue(json)
    println(back)
}

//[ {
//  "type" : "dog",
//  "name" : "멍멍이",
//  "barkVolume" : 5
//}, {
//  "type" : "cat",
//  "name" : "야옹이",
//  "lives" : 9
//} ]