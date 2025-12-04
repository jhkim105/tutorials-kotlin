package jhkim105.tutorials.jwt

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/jwks")
class JwkController(private val jwtService: JwtService) {

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun jwks(): String = jwtService.jwks()

    @GetMapping(value = ["/public-key"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getPublicKey(): String = jwtService.getPublicKeyPEM()
}
