package jhkim105.tutorials.user

import jhkim105.tutorials.jwt.JwtService
import jhkim105.tutorials.security.UserPrincipal
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/login")
class LoginController(
    private val authenticationManager: AuthenticationManager,
    private val userService: UserService,
    private val jwtService: JwtService,
) {

    @PostMapping
    fun login(@RequestBody loginRequest: LoginRequest): LoginResponse {
        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(loginRequest.username, loginRequest.password),
        )
        val userDetails = authentication.principal as UserDetails
        val user = userService.getByUsername(userDetails.username) ?: throw IllegalStateException("User not found")
        val authToken = jwtService.issueToken(UserPrincipal(user))
        return LoginResponse(authToken)
    }
}
