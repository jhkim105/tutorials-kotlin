package jhkim105.tutorials.jwt.ecdsa

import com.nimbusds.jose.JOSEException
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jose.crypto.ECDSAVerifier
import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.gen.ECKeyGenerator
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import com.nimbusds.jwt.util.DateUtils
import jhkim105.tutorials.jwt.JwtPrincipal
import org.bouncycastle.openssl.jcajce.JcaPEMWriter
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Component
import java.io.StringWriter
import java.text.ParseException
import java.util.Base64
import java.util.Date
import java.util.UUID

@Component
class ECUtils : InitializingBean {

    private companion object {
        const val DEFAULT_SKEW_SECONDS: Long = 30
    }

    private lateinit var jwk: ECKey

    fun generateToken(jwtPrincipal: JwtPrincipal): String {
        val header = JWSHeader.Builder(JWSAlgorithm.ES256)
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
            signedJWT.sign(ECDSASigner(jwk))
            return signedJWT.serialize()
        } catch (e: JOSEException) {
            throw RuntimeException(e)
        }
    }

    fun jwks(): String = JWKSet(jwk.toPublicJWK()).toString()

    fun getPublicKeyEncodedString(): String = try {
        Base64.getEncoder().encodeToString(jwk.toPublicJWK().toPublicKey().encoded)
    } catch (e: JOSEException) {
        throw RuntimeException(e)
    }

    fun getPublicKeyPEM(): String = try {
        StringWriter().use { stringWriter ->
            JcaPEMWriter(stringWriter).use { pemWriter ->
                pemWriter.writeObject(jwk.toPublicJWK().toECPublicKey())
                pemWriter.flush()
                stringWriter.toString()
            }
        }
    } catch (e: Exception) {
        throw RuntimeException("Error converting public key to PEM format", e)
    }

    override fun afterPropertiesSet() {
        jwk = ECKey.parse(generateKey())
    }

    fun generateKey(): String = try {
        ECKeyGenerator(Curve.P_256)
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

    private fun parseSignedJWT(token: String): SignedJWT = try {
        SignedJWT.parse(token)
    } catch (e: ParseException) {
        throw IllegalStateException(e)
    }

    private fun parseJwtPrincipal(signedJWT: SignedJWT): JwtPrincipal = try {
        val jwtClaimsSet = signedJWT.jwtClaimsSet
        JwtPrincipal(jwtClaimsSet.subject, jwtClaimsSet.getStringClaim("authority"))
    } catch (e: ParseException) {
        throw IllegalStateException(e)
    }

    private fun verify(signedJWT: SignedJWT) {
        try {
            verifySignature(signedJWT, jwk)
            verifyExpirationTime(signedJWT)
        } catch (e: Exception) {
            throw IllegalStateException(e)
        }
    }

    private fun verifySignature(signedJWT: SignedJWT, publicKey: ECKey) {
        try {
            if (!signedJWT.verify(ECDSAVerifier(publicKey))) {
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
