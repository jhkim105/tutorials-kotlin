package jhkim105.tutorials.kotlin.spring_boot_2

import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class SpringBoot2ApplicationTests {

    private val log = LoggerFactory.getLogger(javaClass)

    @Autowired
    lateinit var serviceProperties: ServiceProperties

    @Test
    fun contextLoads() {
        log.info("$serviceProperties")
    }

}
