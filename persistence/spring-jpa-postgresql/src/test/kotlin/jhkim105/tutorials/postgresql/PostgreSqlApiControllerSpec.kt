package jhkim105.tutorials.postgresql

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@SpringBootTest
@AutoConfigureMockMvc
class PostgreSqlApiControllerSpec(
    private val mockMvc: MockMvc,
    private val productRepository: ProductRepository,
    private val jobTaskRepository: JobTaskRepository
) : DescribeSpec() {

    override fun extensions() = listOf(SpringExtension)

    init {
        describe("ProductController API") {
            beforeEach {
                productRepository.deleteAll()
            }

            it("대량 Batch Insert API 호출 시 지정된 개수만큼 일괄 생성되어야 한다") {
                mockMvc.post("/api/products/batch") {
                    param("count", "100")
                    contentType = MediaType.APPLICATION_JSON
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.requestedCount") { value(100) }
                    jsonPath("$.savedCount") { value(100) }
                }

                productRepository.count() shouldBe 100L
            }

            it("JSONB 검색 API 및 Array 검색 API가 정상 동작해야 한다") {
                val product = Product(
                    name = "MacBook Pro",
                    price = 3000000L,
                    attributes = mapOf("brand" to "Apple", "chip" to "M3"),
                    tags = listOf("apple", "laptop")
                )
                productRepository.save(product)

                mockMvc.get("/api/products/search/jsonb") {
                    param("key", "brand")
                    param("value", "Apple")
                }.andExpect {
                    status { isOk() }
                    jsonPath("$[0].name") { value("MacBook Pro") }
                }

                mockMvc.get("/api/products/search/tag") {
                    param("tag", "laptop")
                }.andExpect {
                    status { isOk() }
                    jsonPath("$[0].name") { value("MacBook Pro") }
                }
            }
        }

        describe("JobTaskController API") {
            beforeEach {
                jobTaskRepository.deleteAll()
            }

            it("Seed 생성 후 claim API 호출 시 순차적으로 작업이 선점되어야 한다") {
                mockMvc.post("/api/jobs/seed") {
                    param("count", "3")
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.createdCount") { value(3) }
                }

                mockMvc.post("/api/jobs/claim") {
                    param("workerName", "TestWorker-1")
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.success") { value(true) }
                    jsonPath("$.task.status") { value("PROCESSING") }
                }

                mockMvc.get("/api/jobs/status").andExpect {
                    status { isOk() }
                    jsonPath("$.totalCount") { value(3) }
                    jsonPath("$.statusSummary.PROCESSING") { value(1) }
                    jsonPath("$.statusSummary.PENDING") { value(2) }
                }
            }
        }
    }
}
