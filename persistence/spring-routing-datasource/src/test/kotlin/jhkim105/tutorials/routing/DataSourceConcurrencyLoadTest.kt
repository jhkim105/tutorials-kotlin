package jhkim105.tutorials.routing

import com.zaxxer.hikari.HikariDataSource
import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import jakarta.persistence.EntityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.hibernate.Session
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.stereotype.Service
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.atomic.AtomicInteger

private val logger = KotlinLogging.logger {}

@Service
class SlowQueryService(
    private val entityManager: EntityManager,
) {
    @Transactional
    fun slowWriteOperation(durationMs: Long = 50): String {
        val session = entityManager.unwrap(Session::class.java)
        var dbUrl = ""
        session.doWork { conn ->
            Thread.sleep(durationMs)
            conn.createStatement().use { stmt ->
                stmt.execute("SELECT 1")
            }
            dbUrl = conn.metaData.url
        }
        return dbUrl
    }

    @Transactional(readOnly = true)
    fun slowReadOperation(durationMs: Long = 50): String {
        val session = entityManager.unwrap(Session::class.java)
        var dbUrl = ""
        session.doWork { conn ->
            Thread.sleep(durationMs)
            conn.createStatement().use { stmt ->
                stmt.execute("SELECT 1")
            }
            dbUrl = conn.metaData.url
        }
        return dbUrl
    }
}

@SpringBootTest
@ActiveProfiles("test")
class DataSourceConcurrencyLoadTest(
    private val slowQueryService: SlowQueryService,
    @Qualifier("masterDataSource") private val masterDataSource: HikariDataSource,
    @Qualifier("slaveDataSources") private val slaveDataSources: Map<String, HikariDataSource>,
) : BehaviorSpec() {

    override fun extensions() = listOf(SpringExtension)

    init {
        val slave1DataSource = slaveDataSources["slave1"] ?: error("slave1 not found")
        val slave2DataSource = slaveDataSources["slave2"] ?: error("slave2 not found")

        Given("동시에 대량의 쓰기 및 읽기 요청이 발생하는 환경에서") {

            When("쓰기 20건과 읽기 40건의 동시 트랜잭션 요청을 발생시키면") {
                val writeRequestCount = 20
                val readRequestCount = 40

                val masterSuccessCount = AtomicInteger(0)
                val slave1SuccessCount = AtomicInteger(0)
                val slave2SuccessCount = AtomicInteger(0)

                runBlocking(Dispatchers.IO) {
                    val writeJobs = (1..writeRequestCount).map {
                        async {
                            val url = slowQueryService.slowWriteOperation(durationMs = 50)
                            if (url.contains("master_db")) {
                                masterSuccessCount.incrementAndGet()
                            }
                        }
                    }

                    val readJobs = (1..readRequestCount).map {
                        async {
                            val url = slowQueryService.slowReadOperation(durationMs = 50)
                            if (url.contains("slave1_db")) {
                                slave1SuccessCount.incrementAndGet()
                            }
                            if (url.contains("slave2_db")) {
                                slave2SuccessCount.incrementAndGet()
                            }
                        }
                    }

                    // 모든 작업 병렬 대기
                    (writeJobs + readJobs).awaitAll()
                }

                Then("쓰기 요청 20건은 모두 Master 풀(master_db)에서 처리된다") {
                    masterSuccessCount.get() shouldBe writeRequestCount
                }

                Then("읽기 요청 40건은 Slave1과 Slave2 풀(slave1_db, slave2_db)로 균등하게 분산 처리된다") {
                    val totalSlaveProcessed = slave1SuccessCount.get() + slave2SuccessCount.get()
                    totalSlaveProcessed shouldBe readRequestCount

                    // 라운드로빈 로드밸런싱으로 두 Slave에 각각 20건씩 분산됨
                    slave1SuccessCount.get() shouldBe 20
                    slave2SuccessCount.get() shouldBe 20
                }

                Then("각 데이터소스의 독립된 풀(HikariPool)에서 정상적으로 커넥션을 대여 및 반납 완료한다") {
                    logger.info { "Master Pool Active: ${masterDataSource.hikariPoolMXBean?.activeConnections}" }
                    logger.info { "Slave1 Pool Active: ${slave1DataSource.hikariPoolMXBean?.activeConnections}" }
                    logger.info { "Slave2 Pool Active: ${slave2DataSource.hikariPoolMXBean?.activeConnections}" }

                    // 부하 작업 종료 후 활성 커넥션이 0으로 정상 반납되었는지 검증
                    masterDataSource.hikariPoolMXBean?.activeConnections shouldBe 0
                    slave1DataSource.hikariPoolMXBean?.activeConnections shouldBe 0
                    slave2DataSource.hikariPoolMXBean?.activeConnections shouldBe 0
                }
            }
        }
    }
}
