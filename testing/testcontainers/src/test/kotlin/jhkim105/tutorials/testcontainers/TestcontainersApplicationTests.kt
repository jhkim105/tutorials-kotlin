package jhkim105.tutorials.testcontainers

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@Import(TestcontainersConfiguration::class)
@SpringBootTest
class TestcontainersApplicationTests {

    @Test
    fun contextLoads() {
    }

}
