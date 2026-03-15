package jhkim105.tutorials.kotlin

import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class SpringPropertiesApplicationTests {
    private val log = LoggerFactory.getLogger(javaClass)

    @Autowired
    lateinit var serviceProperties: ServiceProperties

    @Autowired
    lateinit var fooProperties: FooProperties


    @Test
    fun contextLoads() {
    }

    @Test
    fun properties() {
        log.info("$serviceProperties")
        log.info("$fooProperties")
        log.info("${fooProperties.bar}")
    }

}
