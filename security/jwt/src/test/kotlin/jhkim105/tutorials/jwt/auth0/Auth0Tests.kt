package jhkim105.tutorials.jwt.auth0

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTCreationException
import com.auth0.jwt.exceptions.JWTVerificationException
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

class Auth0Tests {

    private val log = LoggerFactory.getLogger(javaClass)

    private val secret = "jhkim105"
    private val issuer = "jhkim105"

    @Test
    fun createToken() {
        val token = try {
            val algorithm = Algorithm.HMAC256(secret)
            JWT.create()
                .withIssuer(issuer)
                .withSubject("Subject 1")
                .withClaim("claim1", "value 1")
                .sign(algorithm)
        } catch (e: JWTCreationException) {
            throw RuntimeException(e)
        }
        log.debug("{}", token)
    }

    @Test
    fun parseToken() {
        val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJqaGtpbTEwNSIsInN1YiI6IlN1YmplY3QgMSIsImNsYWltMSI6InZhbHVlIDEifQ.bIo01fNNG5mmKtt-ip7p8rec6q3EDho7-fQIqxJViLs"

        val algorithm = Algorithm.HMAC256(secret)
        val verifier: JWTVerifier = JWT.require(algorithm)
            .withIssuer(issuer)
            .build()
        try {
            val decodedJWT = verifier.verify(token)
            log.debug("Issuer: {}", decodedJWT.issuer)
            log.debug("Subject: {}", decodedJWT.subject)
            log.debug("claim1: {}", decodedJWT.getClaim("claim1"))
            log.debug("expireAt: {}", decodedJWT.expiresAt)
        } catch (e: JWTVerificationException) {
            throw RuntimeException(e)
        }
    }
}
