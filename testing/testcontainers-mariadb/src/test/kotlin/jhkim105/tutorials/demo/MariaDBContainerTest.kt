package jhkim105.tutorials.demo;


import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MariaDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@Testcontainers
@ActiveProfiles("mariadb")
class MariaDBContainerTest {

    @Autowired
    lateinit var userRepository: UserRepository

    @Test
    fun save() {
        userRepository.save(User(name = "user 01"))
    }

    companion object {

        @Container
        @JvmStatic
        val mariaDB = MariaDBContainer("mariadb:10.11.9")
            .withCommand(
                "--character-set-server=utf8mb4",
                "--collation-server=utf8mb4_unicode_ci",
                "--lower_case_table_names=1"
            )

        @JvmStatic
        @DynamicPropertySource
        fun mariaDBProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", mariaDB::getJdbcUrl)
            registry.add("spring.datasource.username", mariaDB::getUsername)
            registry.add("spring.datasource.password", mariaDB::getPassword)
        }
    }
}
