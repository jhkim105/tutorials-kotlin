package jhkim105.tutorials.websocket.server.auth

import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class AuthController(
    private val jwtTokenProvider: JwtTokenProvider
) {
    @PostMapping("/token")
    fun getToken(@RequestBody request: AuthRequest): TokenResponse {
        val token = jwtTokenProvider.generateToken(request.username)
        return TokenResponse(token)
    }

    data class AuthRequest(val username: String)
    data class TokenResponse(val token: String)
}
