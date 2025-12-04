package jhkim105.tutorials.jwt.eddsa

import com.nimbusds.jose.JOSEException
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.Ed25519Signer
import com.nimbusds.jose.crypto.Ed25519Verifier
import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.OctetKeyPair
import com.nimbusds.jose.jwk.gen.OctetKeyPairGenerator
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import com.nimbusds.jwt.util.DateUtils
import jhkim105.tutorials.jwt.JwtPrincipal
import org.bouncycastle.util.io.pem.PemObject
import org.bouncycastle.util.io.pem.PemWriter
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Component
import java.io.StringWriter
import java.text.ParseException
import java.util.Date
import java.util.UUID

@Component
class EdDsaUtils : InitializingBean {

    private companion object {
        const val DEFAULT_SKEW_SECONDS: Long = 30
    }

    private lateinit var jwk: OctetKeyPair

    fun generateToken(jwtPrincipal: JwtPrincipal): String {
        val header = JWSHeader.Builder(JWSAlgorithm.EdDSA)
            .keyID(jwk.keyID)
            .build()

        val claimsSet = JWTClaimsSet.Builder()
            .subject(jwtPrincipal.id)
            .issuer("jhkim105")
            .expirationTime(Date(System.currentTimeMillis() + 60_000))
            .claim("authority", jwtPrincipal.authority)
            .build()

        val signedJWT = SignedJWT(header, claimsSet)
        try {
            signedJWT.sign(Ed25519Signer(jwk.toOctetKeyPair()))
            return signedJWT.serialize()
        } catch (e: JOSEException) {
            throw RuntimeException(e)
        }
    }

    fun jwks(): String = JWKSet(jwk.toPublicJWK()).toString()

    fun getPublicKeyPEM(): String = try {
        StringWriter().use { stringWriter ->
            PemWriter(stringWriter).use { pemWriter ->
                val publicKeyBytes = jwk.decodedX
                val pemObject = PemObject("PUBLIC KEY", publicKeyBytes)
                pemWriter.writeObject(pemObject)
                pemWriter.flush()
                stringWriter.toString()
            }
        }
    } catch (e: Exception) {
        throw RuntimeException("Error converting public key to PEM format", e)
    }

    override fun afterPropertiesSet() {
        jwk = OctetKeyPair.parse(generateJwtKey())
    }

    fun generateJwtKey(): String = try {
        OctetKeyPairGenerator(Curve.Ed25519)
            .keyID(UUID.randomUUID().toString())
            .issueTime(Date())
            .generate()
            .toJSONString()
    } catch (e: JOSEException) {
        throw RuntimeException(e)
    }

    fun parse(token: String): JwtPrincipal {
        val signedJWT = parseSignedJWT(token)
        verify(signedJWT)
        return parseJwtPrincipal(signedJWT)
    }

    private fun parseJwtPrincipal(signedJWT: SignedJWT): JwtPrincipal = try {
        val jwtClaimsSet = signedJWT.jwtClaimsSet
        JwtPrincipal(jwtClaimsSet.subject, jwtClaimsSet.getStringClaim("authority"))
    } catch (e: ParseException) {
        throw IllegalStateException(e)
    }

    private fun parseSignedJWT(token: String): SignedJWT = try {
        SignedJWT.parse(token)
    } catch (e: ParseException) {
        throw IllegalStateException(e)
    }

    private fun verify(signedJWT: SignedJWT) {
        try {
            verifySignature(signedJWT, jwk.toPublicJWK())
            verifyExpirationTime(signedJWT)
        } catch (e: Exception) {
            throw IllegalStateException(e)
        }
    }

    private fun verifySignature(signedJWT: SignedJWT, publicKey: OctetKeyPair) {
        try {
            if (!signedJWT.verify(Ed25519Verifier(publicKey))) {
                throw IllegalStateException("token verification failed")
            }
        } catch (e: Exception) {
            if (e is IllegalStateException) throw e
            throw IllegalStateException(e)
        }
    }

    private fun verifyExpirationTime(signedJWT: SignedJWT) {
        val expiration = try {
            signedJWT.jwtClaimsSet.expirationTime
        } catch (e: ParseException) {
            throw IllegalStateException(e)
        }
        if (!DateUtils.isAfter(expiration, Date(), DEFAULT_SKEW_SECONDS)) {
            throw IllegalStateException("token expired")
        }
    }
}
