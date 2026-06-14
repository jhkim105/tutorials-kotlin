package jhkim105.tutorials.clickhouse.repository

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import jhkim105.tutorials.clickhouse.domain.ProductViewEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.math.BigDecimal
import java.util.UUID

import org.springframework.jdbc.core.JdbcTemplate

@SpringBootTest
class ProductViewEventRepositorySpec @Autowired constructor(
    private val repository: ProductViewEventRepository,
    private val jdbcTemplate: JdbcTemplate
) : BehaviorSpec({

    beforeSpec {
        jdbcTemplate.execute("TRUNCATE TABLE product_view_events")
    }

    Given("ClickHouse에 상품 조회 이벤트 저장 및 조회 검증") {
        val event = ProductViewEvent(
            id = UUID.randomUUID(),
            productId = "PROD-100",
            userId = "USER-99",
            price = BigDecimal("49000.00"),
            urlPath = "/products/PROD-100",
            referrer = "google"
        )

        When("새로운 이벤트를 저장하면") {
            repository.save(event)

            Then("ID로 개별 조회가 가능하며 데이터가 일치해야 한다") {
                val found = repository.findById(event.id)
                found.shouldNotBeNull()
                found.productId shouldBe event.productId
                found.userId shouldBe event.userId
                found.price shouldBe event.price
                found.urlPath shouldBe event.urlPath
                found.referrer shouldBe event.referrer
            }
        }

        When("이벤트를 삭제하면") {
            repository.delete(event.id)

            Then("더 이상 조회되지 않아야 한다") {
                repository.findById(event.id) shouldBe null
            }
        }
    }

    Given("ClickHouse의 OLAP 집계 분석 기능 검증") {
        val prodA = "PROD-A"
        val prodB = "PROD-B"
        val prodC = "PROD-C"

        val events = listOf(
            ProductViewEvent(productId = prodA, userId = "user1", price = BigDecimal("1000.00"), urlPath = "/a", referrer = "google"),
            ProductViewEvent(productId = prodA, userId = "user2", price = BigDecimal("1000.00"), urlPath = "/a", referrer = "naver"),
            ProductViewEvent(productId = prodA, userId = "user3", price = BigDecimal("1000.00"), urlPath = "/a", referrer = "google"),
            ProductViewEvent(productId = prodB, userId = "user4", price = BigDecimal("2000.00"), urlPath = "/b", referrer = "naver"),
            ProductViewEvent(productId = prodB, userId = "user5", price = BigDecimal("2000.00"), urlPath = "/b", referrer = "direct"),
            ProductViewEvent(productId = prodC, userId = "user6", price = BigDecimal("3000.00"), urlPath = "/c", referrer = "direct")
        )

        When("여러 상품의 로그를 다수 저장하면") {
            events.forEach { repository.save(it) }

            Then("가장 많이 조회된 인기 상품 상위 2개를 성공적으로 집계할 수 있어야 한다") {
                val topProducts = repository.getTopViewedProducts(2)

                topProducts shouldHaveSize 2
                // PROD-A는 3번 조회되었으므로 1등이어야 함
                topProducts[0].first shouldBe prodA
                topProducts[0].second shouldBe 3L

                // PROD-B는 2번 조회되었으므로 2등이어야 함
                topProducts[1].first shouldBe prodB
                topProducts[1].second shouldBe 2L
            }
        }
    }
})
