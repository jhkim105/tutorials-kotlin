package jhkim105.tutorials.jwt.nimbus

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSSigner
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jose.shaded.gson.annotations.SerializedName
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.security.SecureRandom
import java.util.Date

class NimbusTests {

    private val log = LoggerFactory.getLogger(javaClass)

    @Test
    fun hs256() {
        val random = SecureRandom()
        val sharedSecret = ByteArray(32)
        random.nextBytes(sharedSecret)

        val signer: JWSSigner = MACSigner(sharedSecret)

        val claimsSet = JWTClaimsSet.Builder()
            .subject("alice")
            .issuer("https://c2id.com")
            .claim("custom", Custom("name001"))
            .expirationTime(Date(System.currentTimeMillis() + 60_000))
            .build()

        var signedJWT = SignedJWT(JWSHeader(JWSAlgorithm.HS256), claimsSet)

        signedJWT.sign(signer)

        val token = signedJWT.serialize()
        log.debug(token)

        signedJWT = SignedJWT.parse(token)
        log.debug(signedJWT.payload.toString())
        assertThat(signedJWT.jwtClaimsSet.subject).isEqualTo("alice")
    }

    data class Custom(
        @SerializedName("custom_name")
        val customName: String
    )
}
