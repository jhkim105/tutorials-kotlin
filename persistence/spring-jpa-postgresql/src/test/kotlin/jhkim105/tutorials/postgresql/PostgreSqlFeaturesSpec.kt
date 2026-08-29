package jhkim105.tutorials.postgresql

import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.support.TransactionTemplate
import java.util.concurrent.ConcurrentHashMap
import kotlin.system.measureTimeMillis

private val log = KotlinLogging.logger {}

@SpringBootTest
class PostgreSqlFeaturesSpec(
    private val productRepository: ProductRepository,
    private val jobTaskRepository: JobTaskRepository,
    private val jobTaskService: JobTaskService,
    private val transactionTemplate: TransactionTemplate
) : BehaviorSpec() {

    override fun extensions() = listOf(SpringExtension)

    init {
        Given("PostgreSQL 특화 기능 테스트 환경") {

            When("1. JSONB 및 Native Array (text[]) 컬럼을 저장하고 조회할 때") {
                productRepository.deleteAll()

                val laptop = Product(
                    name = "MacBook Pro 16",
                    price = 3500000L,
                    attributes = mapOf(
                        "brand" to "Apple",
                        "specs" to mapOf("cpu" to "M3 Max", "ram" to 64)
                    ),
                    tags = listOf("apple", "laptop", "developer")
                )

                val monitor = Product(
                    name = "Dell UltraSharp 32",
                    price = 1200000L,
                    attributes = mapOf(
                        "brand" to "Dell",
                        "resolution" to "4K"
                    ),
                    tags = listOf("dell", "monitor", "4k")
                )

                productRepository.saveAll(listOf(laptop, monitor))

                Then("JSONB 내부 필드(brand=Apple)로 정확히 조회되어야 한다") {
                    val results = productRepository.findByAttributeKeyAndValue("brand", "Apple")
                    results shouldHaveSize 1
                    results.first().name shouldBe "MacBook Pro 16"
                    results.first().attributes["brand"] shouldBe "Apple"
                }

                Then("Native Array 연산자(ANY)로 태그가 포함된 상품을 조인 없이 조회할 수 있어야 한다") {
                    val developerItems = productRepository.findByTag("developer")
                    developerItems shouldHaveSize 1
                    developerItems.first().tags shouldContainExactly listOf("apple", "laptop", "developer")

                    val monitorItems = productRepository.findByTag("monitor")
                    monitorItems shouldHaveSize 1
                    monitorItems.first().name shouldBe "Dell UltraSharp 32"
                }
            }

            When("2. SEQUENCE 기반 대량 Batch Insert를 수행할 때") {
                productRepository.deleteAll()
                val batchCount = 500
                val products = (1..batchCount).map { i ->
                    Product(
                        name = "Product-$i",
                        price = 1000L * i,
                        attributes = mapOf("index" to i),
                        tags = listOf("batch", "tag-$i")
                    )
                }

                val elapsedTime = measureTimeMillis {
                    transactionTemplate.execute {
                        productRepository.saveAll(products)
                    }
                }

                log.info { ">> Batch insert of $batchCount entities completed in ${elapsedTime}ms" }

                Then("모든 엔티티가 정상적으로 일괄 저장되어야 한다") {
                    productRepository.count() shouldBe batchCount.toLong()
                }
            }

            When("3. SKIP LOCKED를 활용해 여러 워커가 동시에 PENDING 작업을 선점할 때") {
                jobTaskRepository.deleteAll()

                val totalTasks = 20
                val tasks = (1..totalTasks).map { i ->
                    JobTask(taskName = "Task-$i", status = TaskStatus.PENDING)
                }
                jobTaskRepository.saveAll(tasks)

                val workerCount = 5
                val claimedTasksMap = ConcurrentHashMap<String, MutableList<Long>>()

                val executionTime = measureTimeMillis {
                    val deferreds = (1..workerCount).map { workerId ->
                        val workerName = "Worker-$workerId"
                        claimedTasksMap[workerName] = mutableListOf()

                        async(Dispatchers.IO) {
                            while (true) {
                                val task = jobTaskService.claimNextTask(workerName) ?: break
                                claimedTasksMap[workerName]?.add(task.id!!)
                            }
                        }
                    }
                    deferreds.awaitAll()
                }

                val allClaimedTaskIds = claimedTasksMap.values.flatten()
                log.info { ">> $totalTasks tasks claimed by $workerCount workers in ${executionTime}ms. Total processed: ${allClaimedTaskIds.size}" }

                Then("동시성 충돌(락 대기/블로킹) 없이 모든 작업이 중복 없이 정확히 1번씩 처리되어야 한다") {
                    allClaimedTaskIds shouldHaveSize totalTasks
                    allClaimedTaskIds.toSet() shouldHaveSize totalTasks // 중복 선점 없음 검증
                    jobTaskRepository.count() shouldBe totalTasks.toLong()
                }
            }
        }
    }
}
