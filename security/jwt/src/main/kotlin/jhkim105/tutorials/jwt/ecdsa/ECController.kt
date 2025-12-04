package jhkim105.tutorials.jwt.ecdsa

import jhkim105.tutorials.jwt.JwtPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/ecdsa")
class ECController(
    private val ecUtils: ECUtils
) {

    @GetMapping("/generate")
    fun generate(): String = ecUtils.generateToken(JwtPrincipal(UUID.randomUUID().toString(), "USER"))

    @GetMapping("/parse")
    fun parse(token: String): JwtPrincipal = ecUtils.parse(token)

    @GetMapping("/jwks")
    fun jwks(): String = ecUtils.jwks()

    // https://jwt.io 에서 검증시 사용
    @GetMapping("/public-key")
    fun publicKey(): String = ecUtils.getPublicKeyPEM()
}
