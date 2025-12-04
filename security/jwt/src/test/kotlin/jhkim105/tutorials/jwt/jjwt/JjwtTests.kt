package jhkim105.tutorials.jwt.jjwt

import com.fasterxml.jackson.annotation.JsonProperty
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Encoders
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import javax.crypto.SecretKey

class JjwtTests {

    private val log = LoggerFactory.getLogger(javaClass)

    @Test
    fun test() {
        val key: SecretKey = Jwts.SIG.HS256.key().build()

        val jws = Jwts.builder()
            .subject("Joe")
            .claim("custom", Custom("name123"))
            .signWith(key)
            .compact()
        log.info("{}", jws)
        assertThat(Jwts.parser().verifyWith(key).build().parseSignedClaims(jws).payload.subject)
            .isEqualTo("Joe")

        val secretString = Encoders.BASE64.encode(key.encoded)
        log.info("{}", secretString)
    }

    data class Custom(
        @JsonProperty("custom_name")
        val customName: String
    )
}
