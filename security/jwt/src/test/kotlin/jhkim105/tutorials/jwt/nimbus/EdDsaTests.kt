package jhkim105.tutorials.jwt.nimbus

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSSigner
import com.nimbusds.jose.JWSVerifier
import com.nimbusds.jose.crypto.Ed25519Signer
import com.nimbusds.jose.crypto.Ed25519Verifier
import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.OctetKeyPair
import com.nimbusds.jose.jwk.gen.OctetKeyPairGenerator
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.util.Date

/**
 * https://connect2id.com/products/nimbus-jose-jwt/examples/jwt-with-eddsa
 */
class EdDsaTests {

    private val log = LoggerFactory.getLogger(javaClass)

    @Test
    fun generateAndParseKeyPair() {
        val jwk = OctetKeyPairGenerator(Curve.Ed25519)
            .keyID("123")
            .generate()
        val keyString = jwk.toString()

        log.debug(keyString)
        val parsedJwk = OctetKeyPair.parse(keyString)
        assertThat(jwk).isEqualTo(parsedJwk)
    }

    @Test
    fun parseKeyPair() {
    }

    @Test
    fun generateAndVerify_EdDSA() {
        val jwk = OctetKeyPairGenerator(Curve.Ed25519)
            .keyID("123")
            .generate()
        val publicJWK = jwk.toPublicJWK()
        log.debug("jwk: {}", jwk)

        val signer: JWSSigner = Ed25519Signer(jwk)

        val claimsSet = JWTClaimsSet.Builder()
            .subject("alice")
            .issuer("https://c2id.com")
            .expirationTime(Date(System.currentTimeMillis() + 60_000))
            .build()

        var signedJWT = SignedJWT(
            JWSHeader.Builder(JWSAlgorithm.EdDSA).keyID(jwk.keyID).build(),
            claimsSet
        )

        signedJWT.sign(signer)

        val token = signedJWT.serialize()
        log.debug("token: {}", token)

        signedJWT = SignedJWT.parse(token)

        val verifier: JWSVerifier = Ed25519Verifier(publicJWK)
        assertThat(signedJWT.verify(verifier)).isTrue()

        assertThat(signedJWT.jwtClaimsSet.subject).isEqualTo("alice")
        assertThat(signedJWT.jwtClaimsSet.issuer).isEqualTo("https://c2id.com")
        assertThat(Date().before(signedJWT.jwtClaimsSet.expirationTime)).isTrue()
    }
}
