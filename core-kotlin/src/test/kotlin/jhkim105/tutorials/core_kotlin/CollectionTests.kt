package jhkim105.tutorials.core_kotlin

import org.junit.jupiter.api.Test


data class Person(val name: String, val age: Int)

class CollectionTests {
    private val personList = listOf(
        Person("Alice", 29),
        Person("Bob", 32),
        Person("Charlie", 31),
        Person("Carol", 32),
    )

    @Test
    fun associate() {
        val map = personList.associate {it.name to it.age}
        println(map)
    }

    @Test
    fun associateBy() {
        val map: Map<String, Person> = personList.associateBy { it.name }
        println(map)
    }


    @Test
    fun associateTo() {
        val map = mutableMapOf<String, Int>()
            personList.associateTo(map) { it.name to it.age }
        println(map)
    }

    @Test
    fun associateByTo() {
        val map = mutableMapOf<String, Person>()
        personList.associateByTo(map) { it.name }
        println(map)
    }

    @Test
    fun associateWith() {
        val map: Map<Person, String> = personList.associateWith { it.name }
        println(map)
    }


    @Test
    fun groupBy() {
        val map: Map<Int, List<Person>> = personList.groupBy { it.age }
        println(map)
    }

}