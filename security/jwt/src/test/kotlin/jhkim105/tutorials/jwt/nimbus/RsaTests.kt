package jhkim105.tutorials.jwt.nimbus

import com.nimbusds.jose.JOSEException
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSSigner
import com.nimbusds.jose.JWSVerifier
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.nio.charset.StandardCharsets
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.NoSuchAlgorithmException
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.Date
import java.util.UUID

/**
 * https://connect2id.com/products/nimbus-jose-jwt/examples/jwt-with-rsa-signature
 */
class RsaTests {

    private val log = LoggerFactory.getLogger(javaClass)

    @Test
    fun test() {
        val keySize = 2048
        val keyId = "key01"

        val rsaKey = RSAKeyGenerator(keySize)
            .keyID(keyId)
            .generate()

        val signer: JWSSigner = RSASSASigner(rsaKey)

        val claimsSet = JWTClaimsSet.Builder()
            .subject("alice")
            .issuer("https://c2id.com")
            .expirationTime(Date(System.currentTimeMillis() + 60_000))
            .build()

        var signedJWT = SignedJWT(
            JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaKey.keyID).build(),
            claimsSet
        )

        signedJWT.sign(signer)

        val token = signedJWT.serialize()
        log.debug("token: {}", token)
        signedJWT = SignedJWT.parse(token)

        val publicJWK = rsaKey.toPublicJWK()
        val verifier: JWSVerifier = RSASSAVerifier(publicJWK)
        assertThat(signedJWT.verify(verifier)).isTrue()

        assertThat(signedJWT.jwtClaimsSet.subject).isEqualTo("alice")
        assertThat(signedJWT.jwtClaimsSet.issuer).isEqualTo("https://c2id.com")
        assertThat(Date().before(signedJWT.jwtClaimsSet.expirationTime)).isTrue()
    }

    @Test
    fun generateJWK() {
        val jwk = RSAKeyGenerator(2048)
            .keyUse(KeyUse.SIGNATURE)
            .keyID(UUID.randomUUID().toString())
            .issueTime(Date())
            .generate()

        log.debug("jwk-> {}", jwk)
        log.debug("publicJWK-> {}", jwk.toPublicJWK())
    }

    @Test
    fun generateJWKUsingKeyPairGenerator() {
        val gen = KeyPairGenerator.getInstance("RSA")
        gen.initialize(2048)
        val keyPair: KeyPair = gen.generateKeyPair()
        val jwk: JWK = RSAKey.Builder(keyPair.public as RSAPublicKey)
            .privateKey(keyPair.private as RSAPrivateKey)
            .keyUse(KeyUse.SIGNATURE)
            .keyID(UUID.randomUUID().toString())
            .issueTime(Date())
            .build()

        log.debug("jwk-> {}", jwk)
        log.debug("publicJWK-> {}", jwk.toPublicJWK())
    }

    @Test
    @Throws(JOSEException::class)
    fun testPemPkcs8() {
        val pkcs1 = readResource("rsa-private.pem")
        val pkcs8 = readResource("rsa-private-pkcs8.pem")

        val jwk1 = JWK.parseFromPEMEncodedObjects(pkcs1)
        val jwk8 = JWK.parseFromPEMEncodedObjects(pkcs8)

        assertThat(jwk1).isNotNull
        assertThat(jwk8).isNotNull
    }

    @Test
    @Throws(Exception::class)
    fun generateAndParseKeyPair() {
        val rsaJwk = RSAKeyGenerator(2048)
            .keyID("rsa-001")
            .generate()

        val jwkString = rsaJwk.toString()
        log.debug("jwk: {}", jwkString)
        val parsedJwk = RSAKey.parse(jwkString)

        assertThat(rsaJwk).isEqualTo(parsedJwk)
    }

    private fun readResource(name: String): String {
        val resource = javaClass.classLoader.getResourceAsStream(name)
            ?: throw IllegalStateException("Resource $name not found")
        return resource.use { String(it.readAllBytes(), StandardCharsets.UTF_8) }
    }
}
