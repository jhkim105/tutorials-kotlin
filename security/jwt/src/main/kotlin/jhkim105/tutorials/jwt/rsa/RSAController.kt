package jhkim105.tutorials.jwt.rsa

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/rsa")
class RSAController(
    private val rsaUtils: RSAUtils
) {

    @GetMapping("/generate")
    fun generate(): String = rsaUtils.generateToken()

    @GetMapping("/jwks")
    fun jwks(): String = rsaUtils.jwks()

    /**
     * https://jwt.io 에서 검증시 사용
     */
    @GetMapping("/public-key")
    fun publicKey(): String = rsaUtils.getPublicKeyPEM()
}
