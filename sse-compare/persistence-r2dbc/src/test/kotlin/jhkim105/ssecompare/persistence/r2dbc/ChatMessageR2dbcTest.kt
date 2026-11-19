package jhkim105.ssecompare.persistence.r2dbc

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import jhkim105.ssecompare.common.MessageRole
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MySQLContainer
import java.util.UUID

@SpringBootApplication
class R2dbcTestApplication

@SpringBootTest(classes = [R2dbcTestApplication::class])
@ActiveProfiles("db-r2dbc")
class ChatMessageR2dbcTest : BehaviorSpec() {
    override fun extensions() = listOf(SpringExtension)

    @Autowired
    lateinit var store: R2dbcChatMessageStore

    @Autowired
    lateinit var repository: ChatMessageR2dbcRepository

    @Autowired
    lateinit var databaseClient: DatabaseClient

    companion object {
        private val mysql = MySQLContainer("mysql:8.4")
            .withDatabaseName("sse_compare")
            .withUsername("sse")
            .withPassword("ssepass")
            .apply { start() }

        @JvmStatic
        @DynamicPropertySource
        fun props(registry: DynamicPropertyRegistry) {
            registry.add("spring.r2dbc.url") {
                "r2dbc:mysql://${mysql.host}:${mysql.firstMappedPort}/${mysql.databaseName}?serverZoneId=UTC"
            }
            registry.add("spring.r2dbc.username", mysql::getUsername)
            registry.add("spring.r2dbc.password", mysql::getPassword)
            registry.add("spring.r2dbc.pool.max-size") { "10" }
        }
    }

    init {
        beforeSpec {
            databaseClient.sql(
                """
                CREATE TABLE IF NOT EXISTS chat_message (
                    id BIGINT NOT NULL AUTO_INCREMENT,
                    stream_id VARCHAR(36) NOT NULL,
                    role VARCHAR(10) NOT NULL,
                    content TEXT NOT NULL,
                    created_at DATETIME(6) NOT NULL,
                    PRIMARY KEY (id),
                    INDEX idx_chat_message_stream_id (stream_id)
                )
                """.trimIndent(),
            ).then().block()
        }

        given("R2dbcChatMessageStore (MySQL Testcontainers)") {
            `when`("suspend save 로 USER 와 ASSISTANT 메시지를 저장하면") {
                then("동일 streamId 로 2건이 순서대로 조회된다") {
                    val streamId = UUID.randomUUID().toString()
                    store.save(streamId, MessageRole.USER, "hello r2dbc")
                    store.save(streamId, MessageRole.ASSISTANT, "chunk-1:xxx")

                    val rows = repository.findByStreamIdOrderByIdAsc(streamId).collectList().block()!!
                    rows shouldHaveSize 2
                    rows[0].role shouldBe MessageRole.USER
                    rows[0].content shouldBe "hello r2dbc"
                    rows[1].role shouldBe MessageRole.ASSISTANT
                    rows[1].content shouldBe "chunk-1:xxx"
                }
            }
        }
    }
}
