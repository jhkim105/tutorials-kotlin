// src/test/kotlin/com/example/demo/UserRepositoryTest.kt
package jhkim105.tutorials.demo

import jhkim105.tutorials.demo.MariaDBContainerTest.Companion.mariaDB
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.MariaDBContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.Duration

@Testcontainers
@SpringBootTest
class UserRepositoryTest {

    @Autowired
    lateinit var userRepository: UserRepository

    companion object {
        @Container
//        @ServiceConnection
        val mariadbContainer = MariaDBContainer("mariadb:10.11.9").apply {
            withExposedPorts(3306)
            withStartupTimeout(Duration.ofSeconds(60)) // 충분한 시간 확보
            withMinimumRunningDuration(Duration.ofSeconds(60))
//            waitingFor(Wait.forLogMessage(".*ready for connections.*\\s", 1))
            waitingFor(Wait.forHealthcheck())
        }


        @JvmStatic
        @DynamicPropertySource
        fun mariaDBProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", mariaDB::getJdbcUrl)
            registry.add("spring.datasource.username", mariaDB::getUsername)
            registry.add("spring.datasource.password", mariaDB::getPassword)
        }
    }

    @Test
    fun `사용자 저장 후 조회`() {
        val user = userRepository.save(User(name = "Alice"))
        val found = userRepository.findById(user.id).get()
        assertEquals("Alice", found.name)
    }
}
