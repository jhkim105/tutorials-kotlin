package jhkim105.tutorials.jwt.nimbus

import com.nimbusds.jose.JOSEException
import com.nimbusds.jose.JWSObject
import com.nimbusds.jose.JWSVerifier
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.SignedJWT
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.net.URL
import java.text.ParseException

/**
 *
 * Get Token
 * - curl-token-password-grant-type.sh
 *
 */
@Disabled
class KeycloakTokenVerifyTests {

    private val log = LoggerFactory.getLogger(javaClass)

    @Test
    @Disabled
    fun verify() {
        val token = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJnSVFZNTF3NzZRamkzVE9uR2JPWmd2YUdBSnhnOGw0cFVJTFZqZ0t4SVNvIn0.eyJleHAiOjE2OTAzNTM4NzEsImlhdCI6MTY5MDM1MzU3MSwianRpIjoiNjU1ZGRiZWItMzRmOS00N2JjLTgzZWEtYTYyODgwNWExNmZlIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDg5L3JlYWxtcy9kZW1vIiwiYXVkIjoiYWNjb3VudCIsInN1YiI6IjFmZjliYTA4LThkMDItNDE2Yi04YjkzLWM4YWQwOWQ4MDA3YiIsInR5cCI6IkJlYXJlciIsImF6cCI6Im9pZGMtZGVtbyIsInNlc3Npb25fc3RhdGUiOiI5YTBhZWJmNS0yYzhmLTQ5MWEtYTEzOS1hMTU3YThiZTdmZjciLCJhY3IiOiIxIiwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iLCJkZWZhdWx0LXJvbGVzLWRlbW8iXX0sInJlc291cmNlX2FjY2VzcyI6eyJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6ImVtYWlsIHByb2ZpbGUgcmVhZCIsInNpZCI6IjlhMGFlYmY1LTJjOGYtNDkxYS1hMTM5LWExNTdhOGJlN2ZmNyIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwicHJlZmVycmVkX3VzZXJuYW1lIjoidXNlcjAxIiwiZ2l2ZW5fbmFtZSI6IiIsImZhbWlseV9uYW1lIjoiIn0.gSwp8XGDMFN42ZFoTuq-a-JRrHro0PgruaX897lzPp33VZkSaBAeU0-gJYgLppEsyL7RSX78Hmv053885cDZ2HoaztvZl0yF-RUuWPvgv2BmUmPiA1dytjxYHX1WOSW6TKegjbFFyqU8ryiKRmZ83VoIGyOcLVHrDveHh0tVuMJ-YkRE3rKvghffkDJbbDxB_Ceq6dddgk7XSgosdKarT8BPCOf1-WMpL4OMypct7tIMR9BwEJ_1m2EfZOwkKPkOXdPkj_R7PlfZm5lDhOLyubGxFI_HKBky2DM2dhns9oJ8_hU_j68T9ep6tpqwZcAts6I0h4AgmbKVeldNlsZJOA"

        val jwsObject = JWSObject.parse(token)
        val keyId = jwsObject.header.keyID
        log.debug("payload: {}", jwsObject.payload)

        val jwksUrl = "http://localhost:8089/realms/demo/protocol/openid-connect/certs"
        val jwkSet = getPublicKeyFromUrl(jwksUrl)
        val jwk = jwkSet.getKeyByKeyId(keyId)
        val rsaKey = jwk.toRSAKey()
        val verifier: JWSVerifier = RSASSAVerifier(rsaKey)
        assertThat(jwsObject.verify(verifier)).isTrue()
    }

    @Test
    fun verifyWithSignedJWT() {
        val token = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJnSVFZNTF3NzZRamkzVE9uR2JPWmd2YUdBSnhnOGw0cFVJTFZqZ0t4SVNvIn0.eyJleHAiOjE2OTAzNTM4NzEsImlhdCI6MTY5MDM1MzU3MSwianRpIjoiNjU1ZGRiZWItMzRmOS00N2JjLTgzZWEtYTYyODgwNWExNmZlIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDg5L3JlYWxtcy9kZW1vIiwiYXVkIjoiYWNjb3VudCIsInN1YiI6IjFmZjliYTA4LThkMDItNDE2Yi04YjkzLWM4YWQwOWQ4MDA3YiIsInR5cCI6IkJlYXJlciIsImF6cCI6Im9pZGMtZGVtbyIsInNlc3Npb25fc3RhdGUiOiI5YTBhZWJmNS0yYzhmLTQ5MWEtYTEzOS1hMTU3YThiZTdmZjciLCJhY3IiOiIxIiwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iLCJkZWZhdWx0LXJvbGVzLWRlbW8iXX0sInJlc291cmNlX2FjY2VzcyI6eyJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6ImVtYWlsIHByb2ZpbGUgcmVhZCIsInNpZCI6IjlhMGFlYmY1LTJjOGYtNDkxYS1hMTM5LWExNTdhOGJlN2ZmNyIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwicHJlZmVycmVkX3VzZXJuYW1lIjoidXNlcjAxIiwiZ2l2ZW5fbmFtZSI6IiIsImZhbWlseV9uYW1lIjoiIn0.gSwp8XGDMFN42ZFoTuq-a-JRrHro0PgruaX897lzPp33VZkSaBAeU0-gJYgLppEsyL7RSX78Hmv053885cDZ2HoaztvZl0yF-RUuWPvgv2BmUmPiA1dytjxYHX1WOSW6TKegjbFFyqU8ryiKRmZ83VoIGyOcLVHrDveHh0tVuMJ-YkRE3rKvghffkDJbbDxB_Ceq6dddgk7XSgosdKarT8BPCOf1-WMpL4OMypct7tIMR9BwEJ_1m2EfZOwkKPkOXdPkj_R7PlfZm5lDhOLyubGxFI_HKBky2DM2dhns9oJ8_hU_j68T9ep6tpqwZcAts6I0h4AgmbKVeldNlsZJOA"

        var signedJWT = SignedJWT.parse(token)
        log.debug("payload: {}", signedJWT.payload)
        val keyId = signedJWT.header.keyID
        log.debug("{}", signedJWT.jwtClaimsSet.subject)

        val jwksUrl = "http://localhost:8089/realms/demo/protocol/openid-connect/certs"
        val jwkSet = getPublicKeyFromUrl(jwksUrl)

        val jwk = jwkSet.getKeyByKeyId(keyId)
        val rsaPublicKey = jwk.toRSAKey()
        val verifier: JWSVerifier = RSASSAVerifier(rsaPublicKey)
        assertThat(signedJWT.verify(verifier)).isTrue()
    }

    private fun getPublicKeyFromUrl(jwksUrl: String): JWKSet {
        val connectTimeout = 1000
        val readTimeout = 1000
        val sizeLimit = 10000
        return try {
            val url = URL(jwksUrl)
            JWKSet.load(url, connectTimeout, readTimeout, sizeLimit)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}
