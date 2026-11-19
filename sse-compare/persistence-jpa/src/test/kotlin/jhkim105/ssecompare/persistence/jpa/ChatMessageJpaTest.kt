package jhkim105.ssecompare.persistence.jpa

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import jhkim105.ssecompare.common.MessageRole
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MySQLContainer
import java.util.UUID

@SpringBootApplication
class JpaTestApplication

@SpringBootTest(classes = [JpaTestApplication::class])
@ActiveProfiles("db-jpa")
class ChatMessageJpaTest : BehaviorSpec() {
    override fun extensions() = listOf(SpringExtension)

    @Autowired
    lateinit var store: JpaChatMessageStore

    @Autowired
    lateinit var repository: ChatMessageJpaRepository

    companion object {
        private val mysql = MySQLContainer("mysql:8.4")
            .withDatabaseName("sse_compare")
            .withUsername("sse")
            .withPassword("ssepass")
            .apply { start() }

        @JvmStatic
        @DynamicPropertySource
        fun props(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", mysql::getJdbcUrl)
            registry.add("spring.datasource.username", mysql::getUsername)
            registry.add("spring.datasource.password", mysql::getPassword)
            registry.add("spring.jpa.hibernate.ddl-auto") { "create-drop" }
            registry.add("spring.datasource.hikari.maximum-pool-size") { "10" }
        }
    }

    init {
        given("JpaChatMessageStore (MySQL Testcontainers)") {
            `when`("스트림 시작(USER)과 종료(ASSISTANT) 메시지를 저장하면") {
                then("동일 streamId 로 2건이 순서대로 조회된다") {
                    val streamId = UUID.randomUUID().toString()
                    store.save(streamId, MessageRole.USER, "hello jpa")
                    store.save(streamId, MessageRole.ASSISTANT, "chunk-1:xxx")

                    val rows = repository.findByStreamIdOrderByIdAsc(streamId)
                    rows shouldHaveSize 2
                    rows[0].role shouldBe MessageRole.USER
                    rows[0].content shouldBe "hello jpa"
                    rows[1].role shouldBe MessageRole.ASSISTANT
                    rows[1].content shouldBe "chunk-1:xxx"
                }
            }

            `when`("중도 취소되어 USER 메시지만 저장하면") {
                then("ASSISTANT 행은 저장되지 않고 USER 1건만 존재한다") {
                    val streamId = UUID.randomUUID().toString()
                    store.save(streamId, MessageRole.USER, "cancelled stream")

                    val rows = repository.findByStreamIdOrderByIdAsc(streamId)
                    rows shouldHaveSize 1
                    rows[0].role shouldBe MessageRole.USER
                }
            }
        }
    }
}
