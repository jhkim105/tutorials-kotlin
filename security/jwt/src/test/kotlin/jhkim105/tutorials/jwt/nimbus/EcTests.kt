package jhkim105.tutorials.jwt.nimbus

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSVerifier
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jose.crypto.ECDSAVerifier
import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.jwk.gen.ECKeyGenerator
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier
import com.nimbusds.jose.proc.SecurityContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.util.Base64
import java.util.Date

/**
 * https://connect2id.com/products/nimbus-jose-jwt/examples/jwt-with-ec-signature
 */
class EcTests {

    private val log = LoggerFactory.getLogger(javaClass)

    @Test
    fun generateAndVerify_ES256() {
        val jwk = ECKeyGenerator(Curve.P_256)
            .keyID("123")
            .generate()
        log.debug("jwk: {}", jwk)
        log.debug("privateKey: {}", Base64.getEncoder().encodeToString(jwk.toPrivateKey().encoded))
        log.debug("publicKey: {}", Base64.getEncoder().encodeToString(jwk.toPublicKey().encoded))

        val ecPublicJWK = jwk.toPublicJWK()

        val claimsSet = JWTClaimsSet.Builder()
            .subject("alice")
            .issuer("https://c2id.com")
            .expirationTime(Date(System.currentTimeMillis() + 60_000))
            .build()

        var signedJWT = SignedJWT(
            JWSHeader.Builder(JWSAlgorithm.ES256).keyID(jwk.keyID).build(),
            claimsSet
        )

        signedJWT.sign(ECDSASigner(jwk))

        val token = signedJWT.serialize()
        log.debug("token: {}", token)
        signedJWT = SignedJWT.parse(token)

        val verifier: JWSVerifier = ECDSAVerifier(ecPublicJWK)
        assertThat(signedJWT.verify(verifier)).isTrue()

        assertThat(signedJWT.jwtClaimsSet.subject).isEqualTo("alice")
        assertThat(signedJWT.jwtClaimsSet.issuer).isEqualTo("https://c2id.com")
        assertThat(Date().before(signedJWT.jwtClaimsSet.expirationTime)).isTrue()

        val claimsVerifier = DefaultJWTClaimsVerifier<SecurityContext>(
            JWTClaimsSet.Builder()
                .issuer("https://c2id.com")
                .build(),
            setOf("exp")
        )
        claimsVerifier.verify(signedJWT.jwtClaimsSet, null)
    }

    @Test
    fun generateAndParseKeyPair() {
        val ecJWK = ECKeyGenerator(Curve.P_256)
            .keyID("123")
            .generate()

        val jwkString = ecJWK.toString()
        log.debug("jwk: {}", jwkString)
        val parsedJwk = ECKey.parse(jwkString)

        assertThat(ecJWK).isEqualTo(parsedJwk)
    }
}
