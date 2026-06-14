package jhkim105.tutorials.clickhouse.controller

import io.github.oshai.kotlinlogging.KotlinLogging
import jhkim105.tutorials.clickhouse.domain.ProductViewEvent
import jhkim105.tutorials.clickhouse.repository.ProductViewEventRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.util.UUID

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/api/events")
class ProductViewEventController(
    private val repository: ProductViewEventRepository
) {

    @PostMapping
    fun createEvent(@RequestBody request: ProductViewEventRequest): ResponseEntity<ProductViewEvent> {
        val event = request.toDomain()
        logger.info { "API 호출 - 신규 상품 조회 이벤트 등록 요청: $event" }
        repository.save(event)
        return ResponseEntity.status(HttpStatus.CREATED).body(event)
    }

    @GetMapping("/{id}")
    fun getEvent(@PathVariable id: UUID): ResponseEntity<ProductViewEvent> {
        logger.info { "API 호출 - 이벤트 조회 요청 (ID: $id)" }
        val event = repository.findById(id)
        return if (event != null) {
            ResponseEntity.ok(event)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping
    fun getAllEvents(): ResponseEntity<List<ProductViewEvent>> {
        logger.info { "API 호출 - 전체 이벤트 조회 요청" }
        val events = repository.findAll()
        return ResponseEntity.ok(events)
    }

    @DeleteMapping("/{id}")
    fun deleteEvent(@PathVariable id: UUID): ResponseEntity<Void> {
        logger.info { "API 호출 - 이벤트 삭제 요청 (ID: $id)" }
        repository.delete(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/top")
    fun getTopProducts(
        @RequestParam(defaultValue = "5") limit: Int
    ): ResponseEntity<List<TopViewedProductResponse>> {
        logger.info { "API 호출 - 인기 상품 OLAP 집계 조회 요청 (상위 ${limit}개)" }
        val topList = repository.getTopViewedProducts(limit)
        val response = topList.map { TopViewedProductResponse(it.first, it.second) }
        return ResponseEntity.ok(response)
    }
}

/**
 * 이벤트 생성용 Request DTO
 */
data class ProductViewEventRequest(
    val productId: String,
    val userId: String,
    val price: BigDecimal,
    val urlPath: String,
    val referrer: String
) {
    fun toDomain(): ProductViewEvent = ProductViewEvent(
        productId = productId,
        userId = userId,
        price = price,
        urlPath = urlPath,
        referrer = referrer
    )
}

/**
 * 인기 상품 분석 결과 Response DTO
 */
data class TopViewedProductResponse(
    val productId: String,
    val viewCount: Long
)
