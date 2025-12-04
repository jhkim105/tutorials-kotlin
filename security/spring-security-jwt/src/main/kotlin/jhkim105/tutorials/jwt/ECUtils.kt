package jhkim105.tutorials.jwt

import com.nimbusds.jose.JOSEException
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jose.crypto.ECDSAVerifier
import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.gen.ECKeyGenerator
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import com.nimbusds.jwt.util.DateUtils
import org.bouncycastle.openssl.jcajce.JcaPEMWriter
import java.io.StringWriter
import java.text.ParseException
import java.util.Date
import java.util.HashMap

object ECUtils {

    private const val DEFAULT_SKEW_SECONDS: Long = 30

    fun generateKey(keyId: String): String = try {
        ECKeyGenerator(Curve.P_256)
            .keyID(keyId)
            .issueTime(Date())
            .generate()
            .toJSONString()
    } catch (e: JOSEException) {
        throw RuntimeException(e)
    }

    fun generateToken(key: String, claimsMap: Map<String, Any>): String {
        val ecKey = parseJWK(key)
        val header = JWSHeader.Builder(JWSAlgorithm.ES256)
            .keyID(ecKey.keyID)
            .build()

        val claimsSetBuilder = JWTClaimsSet.Builder()
            .expirationTime(Date(Date().time + 60 * 1000))

        claimsMap.forEach(claimsSetBuilder::claim)

        val signedJWT = SignedJWT(header, claimsSetBuilder.build())
        return try {
            signedJWT.sign(ECDSASigner(ecKey))
            signedJWT.serialize()
        } catch (e: JOSEException) {
            throw RuntimeException(e)
        }
    }

    private fun parseJWK(key: String): ECKey = try {
        ECKey.parse(key)
    } catch (e: ParseException) {
        throw RuntimeException(e)
    }

    fun parseKeyId(token: String): String = try {
        SignedJWT.parse(token).header.keyID
    } catch (e: ParseException) {
        throw RuntimeException(e)
    }

    fun parse(publicKey: String, token: String): Map<String, Any> {
        val signedJWT = parseSignedJWT(token)
        val publicJWK = parseJWK(publicKey)
        verify(publicJWK, signedJWT)
        return parseClaims(signedJWT)
    }

    private fun parseSignedJWT(token: String): SignedJWT = try {
        SignedJWT.parse(token)
    } catch (e: ParseException) {
        throw IllegalStateException(e)
    }

    private fun parseClaims(signedJWT: SignedJWT): Map<String, Any> = try {
        val jwtClaimsSet = signedJWT.jwtClaimsSet
        val claimsMap = HashMap<String, Any>()
        claimsMap["id"] = jwtClaimsSet.getStringClaim("id")
        claimsMap["authority"] = jwtClaimsSet.getStringClaim("authority")
        claimsMap
    } catch (e: ParseException) {
        throw IllegalStateException(e)
    }

    private fun verify(publicJWK: ECKey, signedJWT: SignedJWT) {
        try {
            verifySignature(signedJWT, publicJWK)
            verifyExpirationTime(signedJWT)
        } catch (e: JOSEException) {
            throw IllegalStateException(e)
        } catch (e: ParseException) {
            throw IllegalStateException(e)
        }
    }

    @Throws(JOSEException::class)
    private fun verifySignature(signedJWT: SignedJWT, publicKey: ECKey) {
        if (!signedJWT.verify(ECDSAVerifier(publicKey))) {
            throw IllegalStateException("token verification failed")
        }
    }

    @Throws(ParseException::class)
    private fun verifyExpirationTime(signedJWT: SignedJWT) {
        if (!DateUtils.isAfter(signedJWT.jwtClaimsSet.expirationTime, Date(), DEFAULT_SKEW_SECONDS)) {
            throw IllegalStateException("token expired")
        }
    }

    fun jwks(keyList: List<String>): String {
        val jwks = keyList.map { parseJWK(it).toPublicJWK() }
        return JWKSet(jwks).toString()
    }

    fun getPublicKeyPEM(key: String): String {
        val jwk = parseJWK(key)
        return try {
            StringWriter().use { stringWriter ->
                JcaPEMWriter(stringWriter).use { pemWriter ->
                    pemWriter.writeObject(jwk.toPublicJWK().toECPublicKey())
                }
                stringWriter.toString()
            }
        } catch (e: Exception) {
            throw RuntimeException("Error converting public key to PEM format", e)
        }
    }
}
