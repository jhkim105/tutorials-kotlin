package jhkim105.tutorials.jwt.rsa

import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class RSAUtilsTest @Autowired constructor(
    private val rsaUtils: RSAUtils
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Test
    fun jwks() {
        log.debug(rsaUtils.jwks())
    }

    @Test
    fun getPublicKeyPEM() {
        log.debug(rsaUtils.getPublicKeyPEM())
    }
}
