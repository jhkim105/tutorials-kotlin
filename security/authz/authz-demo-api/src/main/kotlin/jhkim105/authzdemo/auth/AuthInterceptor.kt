package jhkim105.authzdemo.auth

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jhkim105.authzdemo.common.UnauthorizedException
import jhkim105.authzdemo.user.UserRepository
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class AuthInterceptor(
    private val userRepository: UserRepository
) : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val userId = request.getHeader(USER_ID_HEADER)
            ?: throw UnauthorizedException("Missing $USER_ID_HEADER header")

        val user = userRepository.findById(userId)
            ?: throw UnauthorizedException("Unknown user id: $userId")

        request.setAttribute(AUTH_USER_ATTRIBUTE, user)
        return true
    }
}
