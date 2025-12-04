package jhkim105.tutorials.jwt.rsa

import com.nimbusds.jose.JOSEException
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import org.bouncycastle.openssl.jcajce.JcaPEMWriter
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Component
import org.springframework.util.FileCopyUtils
import java.io.StringWriter
import java.util.Base64
import java.util.Date

/**
 * openssl genrsa -out rsa-private.pem 2048
 */
@Component
class RSAUtils(
    private val resourceLoader: ResourceLoader,
    @Value("\${rsa.key-path}")
    private val keyPath: String
) : InitializingBean {

    private lateinit var jwk: RSAKey

    fun generateToken(): String {
        val header = JWSHeader.Builder(JWSAlgorithm.RS256)
            .keyID(jwk.keyID)
            .build()

        val claimsSet = JWTClaimsSet.Builder()
            .subject("rs256")
            .issuer("jhkim105")
            .expirationTime(Date(System.currentTimeMillis() + 60_000))
            .build()

        val signedJWT = SignedJWT(header, claimsSet)
        try {
            signedJWT.sign(RSASSASigner(jwk))
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
                pemWriter.writeObject(jwk.toPublicJWK().toRSAPublicKey())
                pemWriter.flush()
                stringWriter.toString()
            }
        }
    } catch (e: Exception) {
        throw RuntimeException("Error converting public key to PEM format", e)
    }

    override fun afterPropertiesSet() {
        jwk = JWK.parseFromPEMEncodedObjects(readPemEncodedRSAPrivateKeyString()).toRSAKey()
    }

    private fun readPemEncodedRSAPrivateKeyString(): String = try {
        val bytes = FileCopyUtils.copyToByteArray(resourceLoader.getResource(keyPath).inputStream)
        String(bytes)
    } catch (e: Exception) {
        throw RuntimeException(e)
    }
}
