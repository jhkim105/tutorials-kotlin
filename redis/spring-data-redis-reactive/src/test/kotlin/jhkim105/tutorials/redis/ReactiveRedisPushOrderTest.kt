package jhkim105.tutorials.redis

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.ReactiveRedisTemplate
import kotlin.test.Test

@SpringBootTest
class ReactiveRedisPushOrderTest(
    @Autowired val redisTemplate: ReactiveRedisTemplate<String, String>
) {

    private val key = "test:rightPush:singleThread"

    @BeforeEach
    fun clear() {
        redisTemplate.delete(key).block()
    }

    @Test
    fun `단일 스레드에서 subscribe 로 호출 시 저장 순서가 보장되는지 확인`() {
        val messages = (1..10000).map(Int::toString)

        // 단일 스레드에서 순차적으로 subscribe 호출
        messages.forEach { msg ->
            redisTemplate.opsForList()
                .rightPush(key, msg)
                .subscribe() // 비동기 호출
        }
        println("call finished.")
        // 처리 완료까지 대기
        Thread.sleep(10000)

        val result = redisTemplate.opsForList()
            .range(key, 0, -1)
            .collectList()
            .block()

        println("저장된 값: $result")

        // 메시지 순서가 유지되었는지 확인
        Assertions.assertEquals(messages, result, "subscribe 사용 시 순서가 꼬일 수 있음")
    }
}
