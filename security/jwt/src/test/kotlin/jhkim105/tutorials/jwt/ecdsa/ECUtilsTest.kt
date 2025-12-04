package jhkim105.tutorials.jwt.ecdsa

import com.nimbusds.jose.crypto.ECDSAVerifier
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jwt.SignedJWT
import jhkim105.tutorials.jwt.JwtPrincipal
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.UUID

@SpringBootTest
class ECUtilsTest @Autowired constructor(
    private val ecUtils: ECUtils
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Test
    fun verifyUsingPublicKeyPEM() {
        val token = ecUtils.generateToken(JwtPrincipal(UUID.randomUUID().toString(), "USER"))
        log.debug(token)
        val publicKeyPEM = ecUtils.getPublicKeyPEM()
        log.debug(publicKeyPEM)
        log.debug(ecUtils.getPublicKeyEncodedString())
        val publicJWK = JWK.parseFromPEMEncodedObjects(publicKeyPEM)
        val jwt = SignedJWT.parse(token)
        val verifier = ECDSAVerifier(publicJWK as ECKey)

        assertThat(jwt.verify(verifier)).isTrue()
    }

    @Test
    fun generateKey() {
        val key = ecUtils.generateKey()
        val ecKey = ECKey.parse(key)
        log.debug(key)
        log.debug("{}", ecKey.toPublicJWK())
    }
}
