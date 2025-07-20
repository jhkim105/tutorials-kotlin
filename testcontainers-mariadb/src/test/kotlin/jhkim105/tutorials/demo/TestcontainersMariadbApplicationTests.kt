package jhkim105.tutorials.demo

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.context.ImportTestcontainers
import org.springframework.context.annotation.Import

@Import(TestcontainersConfiguration::class)
//@ImportTestcontainers(TestcontainersConfiguration::class)
@SpringBootTest
class TestcontainersMariadbApplicationTests {

    @Test
    fun contextLoads() {
    }

}
