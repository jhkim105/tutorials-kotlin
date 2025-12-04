package jhkim105.tutorials.jwt

import com.nimbusds.jose.jwk.JWKSet
import java.net.URI

object JwksUtils {
    fun getPublicKeyFromUrl(jwksUrl: String): JWKSet {
        val connectTimeout = 1000
        val readTimeout = 1000
        val sizeLimit = 10000
        return try {
            val url = URI(jwksUrl).toURL()
            JWKSet.load(url, connectTimeout, readTimeout, sizeLimit)
        } catch (e: Exception) {
            throw IllegalStateException(e)
        }
    }
}
