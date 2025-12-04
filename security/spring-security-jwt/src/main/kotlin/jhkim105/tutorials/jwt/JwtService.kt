package jhkim105.tutorials.jwt

import jhkim105.tutorials.security.UserPrincipal
import org.springframework.stereotype.Service
import java.util.UUID
import kotlin.text.get

@Service
class JwtService(private val jwkRepository: JwkRepository) {

    fun generateKey(): Jwk {
        val keyId = UUID.randomUUID().toString()
        val key = ECUtils.generateKey(keyId)
        return jwkRepository.save(Jwk(keyId, key))
    }

    fun issueToken(userPrincipal: UserPrincipal): String {
        val jwk = createOrGetJwk()
        val claimMap = mapOf(
            "id" to userPrincipal.id,
            "authority" to userPrincipal.authority,
        )
        return ECUtils.generateToken(jwk.keyData, claimMap)
    }

    private fun createOrGetJwk(): Jwk =
        jwkRepository.findTopByOrderByCreatedAtDesc().orElseGet { generateKey() }

    fun parseToken(token: String): UserPrincipal {
        val keyId = ECUtils.parseKeyId(token)
        val jwk = jwkRepository.findById(keyId).orElseThrow()
        val map = ECUtils.parse(jwk.keyData, token)
        return UserPrincipal(map["id"] as String, map["authority"] as String)
    }

    fun jwks(): String {
        val jwks = jwkRepository.findAll()
        val keyList = jwks.map(Jwk::keyData)
        return ECUtils.jwks(keyList)
    }

    fun getPublicKeyPEM(): String {
        val jwk = createOrGetJwk()
        return ECUtils.getPublicKeyPEM(jwk.keyData)
    }
}
