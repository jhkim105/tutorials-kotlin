package jhkim105.tutorials.redis

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator
import com.fasterxml.jackson.databind.ser.std.StringSerializer
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import jakarta.annotation.Resource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.HashOperations
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.hash.Jackson2HashMapper
import org.springframework.data.redis.hash.ObjectHashMapper
import org.springframework.data.redis.serializer.RedisSerializer
import java.util.stream.Collectors
import kotlin.test.Test

@SpringBootTest
class RedisHashTest(
    @Autowired val redisConnectionFactory: RedisConnectionFactory
) {


    @Test
    fun objectHashMapper() {
        val redisTemplate =  RedisTemplate<String, String>().apply {
            connectionFactory = redisConnectionFactory
            afterPropertiesSet()
        }
        val hashOperations = redisTemplate.opsForHash<ByteArray, ByteArray>()

        val hashMapper = ObjectHashMapper()

        val person = Person("name 1", 21)
        val map = hashMapper.toHash(person) // 변환 (String 변환 X)
        println(map.map { (k, v) -> String(k) to String(v) })
        hashOperations.putAll("person1", map)

        val savedData = hashOperations.entries("person1") // Hash 데이터 가져오기
        val savedPerson = hashMapper.fromHash(savedData)

        savedPerson shouldBe  person
    }

    @Test
    fun jackson2HashMapper() {
        val redisTemplate =  RedisTemplate<String, Any>().apply {
            connectionFactory = redisConnectionFactory
            keySerializer = RedisSerializer.string()
            valueSerializer = RedisSerializer.json()
            hashKeySerializer = RedisSerializer.string()
            hashValueSerializer = RedisSerializer.json()
            afterPropertiesSet()
        }
        val hashOperations = redisTemplate.opsForHash<String, Any>()

        val objectMapper = ObjectMapper().apply {
            registerModule(KotlinModule.Builder().build())

        }
        val hashMapper = Jackson2HashMapper(objectMapper, false)

        val person = Person("name 2", 21)
        val map = hashMapper.toHash(person) // 변환 (String 변환 X)
        hashOperations.putAll("person2", map)

        val savedData = hashOperations.entries("person2") // Hash 데이터 가져오기
        val savedPerson = objectMapper.convertValue(savedData, Person::class.java)

        savedPerson shouldBe  person
    }




    data class Person(val name: String, val age: Int)

}



