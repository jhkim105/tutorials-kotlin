package jhkim105.tutorials.jwt.eddsa

import jhkim105.tutorials.jwt.JwtPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/eddsa")
class EdDsaController(
    private val edDsaUtils: EdDsaUtils
) {

    @GetMapping("/generate")
    fun generate(): String = edDsaUtils.generateToken(JwtPrincipal(UUID.randomUUID().toString(), "USER"))

    @GetMapping("/parse")
    fun parse(token: String): JwtPrincipal = edDsaUtils.parse(token)

    @GetMapping("/jwks")
    fun jwks(): String = edDsaUtils.jwks()

    /**
     * https://jwt.io 에서 검증시 사용
     */
    @GetMapping("/public-key")
    fun publicKey(): String = edDsaUtils.getPublicKeyPEM()
}
