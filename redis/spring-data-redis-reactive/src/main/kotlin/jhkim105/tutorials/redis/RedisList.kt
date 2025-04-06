package jhkim105.tutorials.redis

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/list")
class RedisListController(private val redisListService: RedisListService) {

    @PostMapping
    suspend fun addToList(@RequestBody items: List<String>): Long {
        return redisListService.addToList(items)
    }

    @GetMapping
    suspend fun getList(@RequestParam page: Int, @RequestParam size: Int): List<String> {
        return redisListService.getList(page, size)
    }

    @DeleteMapping("/clear")
    suspend fun clearList(): Boolean {
        return redisListService.clearList()
    }
}

@Service
class RedisListService(
    private val redisTemplate: ReactiveRedisTemplate<String, String>
) {
    private val key = "myList"

    // 리스트에 데이터 추가 (LPUSH)
    suspend fun addToList(items: List<String>): Long {
        return redisTemplate.opsForList().leftPushAll(key, items).awaitSingle()
    }

    // 리스트 데이터 가져오기 (페이지네이션 지원)
    suspend fun getList(page: Int, size: Int): List<String> {
        val start = ((page - 1) * size).toLong()
        val end = start + size - 1
        return redisTemplate.opsForList()
            .range(key, start, end)
            .collectList()
            .awaitSingle()
    }

    // 리스트 전체 삭제
    suspend fun clearList(): Boolean {
        return redisTemplate.delete(key)
            .map { it > 0 }
            .awaitSingle()
    }
}