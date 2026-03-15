package jhkim105.tutorials.demo

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.DockerComposeContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.io.File
import kotlin.test.Test

@SpringBootTest
@Testcontainers
@ActiveProfiles("test", "compose")
class DockerComposeMariaDBContainerTest {



    @Test
    fun contextLoads() {
        println("MariaDB is running on port: ${container.getServicePort("mariadb_1", 3306)}")
    }

    companion object {

        @Container
        @JvmStatic
        val container = DockerComposeContainer(File("src/test/resources/mariadb/docker-compose.yml"))
            .withExposedService("mariadb_1", 3306)

        @JvmStatic
        @DynamicPropertySource
        fun mariaDBProperties(registry: DynamicPropertyRegistry) {
            registry.add("mariadb.port") {
                container.getServicePort("mariadb_1", 3306)
            }
        }
    }
}