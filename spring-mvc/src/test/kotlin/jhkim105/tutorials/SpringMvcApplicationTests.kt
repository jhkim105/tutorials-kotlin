package jhkim105.tutorials

import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class SpringMvcApplicationTests {

    private val log = LoggerFactory.getLogger(javaClass)
    @Test
    fun contextLoads() {
        log.info("SpringMvcApplicationTests")
    }

}
