package jhkim105.tutorials.redis

import io.kotest.core.spec.style.FreeSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import java.util.concurrent.TimeUnit

@SpringBootTest
@Import(RedisTemplateTest.TestConfig::class)
class RedisTemplateTest(
    private val redisTemplate: RedisTemplate<String, String>
) : FreeSpec({

    extensions(SpringExtension)
    "opsForList" - {
        "leftPush" {
            redisTemplate.opsForList().leftPush("list1", "data1")
            redisTemplate.opsForList().leftPush("list1", "data2")
            redisTemplate.expire("list1", 60, TimeUnit.SECONDS)
            val result = redisTemplate.opsForList().range("list1", 0, 1)
            println(result)
            result shouldBe listOf("data2", "data1")
        }
        "rightPush" {
            redisTemplate.opsForList().rightPush("list2", "data1")
            redisTemplate.opsForList().rightPush("list2", "data2")
            redisTemplate.expire("list2", 60, TimeUnit.SECONDS)
            val result = redisTemplate.opsForList().range("list2", 0, 1)
            result shouldBe listOf("data1", "data2")
        }
    }



}) {
    @TestConfiguration
    class TestConfig {

        @Bean
        fun redisTemplate(redisConnectionFactory: RedisConnectionFactory): RedisTemplate<String, String> {
            return RedisTemplate<String, String>().apply {
                connectionFactory = redisConnectionFactory
            }
        }
    }
}



