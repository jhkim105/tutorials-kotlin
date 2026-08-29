package jhkim105.tutorials.postgresql

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import kotlin.system.measureTimeMillis

private val log = KotlinLogging.logger {}

data class BatchInsertResponse(
    val requestedCount: Int,
    val savedCount: Long,
    val elapsedTimeMs: Long,
    val throughputPerSecond: Double
)

@RestController
@RequestMapping("/api/products")
class ProductController(
    private val productRepository: ProductRepository
) {

    /**
     * 1. 대량 Batch Insert 성능 테스트 API
     * SEQUENCE 전략을 통해 Hibernate의 batch_size가 완전히 작동하여 고속 일괄 삽입을 수행합니다.
     */
    @PostMapping("/batch")
    @Transactional
    fun createBatch(
        @RequestParam(defaultValue = "1000") count: Int
    ): ResponseEntity<BatchInsertResponse> {
        val products = (1..count).map { i ->
            Product(
                name = "Item-$i",
                price = (1000..50000).random().toLong(),
                attributes = mapOf(
                    "brand" to if (i % 2 == 0) "Apple" else "Samsung",
                    "category" to "Electronics",
                    "specs" to mapOf("version" to i, "active" to true)
                ),
                tags = listOf("digital", if (i % 2 == 0) "apple" else "samsung", "sale-$i")
            )
        }

        val elapsed = measureTimeMillis {
            productRepository.saveAll(products)
        }

        val totalCount = productRepository.count()
        val throughput = if (elapsed > 0) (count.toDouble() / elapsed) * 1000.0 else 0.0

        log.info { "Batch inserted $count entities in ${elapsed}ms (Throughput: ${String.format("%.2f", throughput)} ops/s)" }

        return ResponseEntity.ok(
            BatchInsertResponse(
                requestedCount = count,
                savedCount = totalCount,
                elapsedTimeMs = elapsed,
                throughputPerSecond = throughput
            )
        )
    }

    /**
     * 2. JSONB 내부 필드 검색 API
     */
    @GetMapping("/search/jsonb")
    fun searchByJsonb(
        @RequestParam key: String,
        @RequestParam value: String
    ): ResponseEntity<List<Product>> {
        val results = productRepository.findByAttributeKeyAndValue(key, value)
        return ResponseEntity.ok(results)
    }

    /**
     * 3. Native Array (text[]) 태그 검색 API
     */
    @GetMapping("/search/tag")
    fun searchByTag(
        @RequestParam tag: String
    ): ResponseEntity<List<Product>> {
        val results = productRepository.findByTag(tag)
        return ResponseEntity.ok(results)
    }
}
