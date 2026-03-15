// src/test/kotlin/com/example/demo/UserRepositoryTest.kt
package jhkim105.tutorials.testcontainers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.Duration

@Testcontainers
@ExtendWith(SpringExtension::class)
@SpringBootTest
class UserRepositoryTest {

    @Autowired
    lateinit var userRepository: UserRepository


    @BeforeEach
    fun beforeEach() {
        mysqlContainer.start()
    }

    companion object {
        @Container
        private val mysqlContainer = MySQLContainer<Nothing>("mysql:8.0.33").apply {
            withDatabaseName("test")
            withUsername("test")
            withPassword("test")
            withExposedPorts(3306)
            withNetworkMode("bridge")
            withStartupTimeout(Duration.ofSeconds(60)) // 충분한 시간 확보
            waitingFor(Wait.forLogMessage(".*ready for connections.*\\s", 1))
        }

        @JvmStatic
        @DynamicPropertySource
        fun overrideProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl)
            registry.add("spring.datasource.username", mysqlContainer::getUsername)
            registry.add("spring.datasource.password", mysqlContainer::getPassword)
        }
    }

    @Test
    fun `사용자 저장 후 조회`() {
        val user = userRepository.save(User(name = "Alice"))
        val found = userRepository.findById(user.id).get()
        assertEquals("Alice", found.name)
    }
}
