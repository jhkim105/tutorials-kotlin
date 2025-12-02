package jhkim105.authzdemo.auth

import jakarta.servlet.http.HttpServletRequest
import jhkim105.authzdemo.common.ForbiddenException
import jhkim105.authzdemo.common.UnauthorizedException
import jhkim105.authzdemo.user.User
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

@Aspect
@Component
class AuthzAspect {

    @Before("@annotation(allowRoles)")
    fun check(allowRoles: AllowRoles) {
        val user = currentUser()
        val allowedRoles = allowRoles.value.toSet()
        if (allowedRoles.isNotEmpty() && user.role !in allowedRoles) {
            throw ForbiddenException("Access denied for role ${user.role}")
        }
    }

    private fun currentUser(): User {
        val request = currentRequest()
        return (request.getAttribute(AUTH_USER_ATTRIBUTE) as? User)
            ?: throw UnauthorizedException("Unauthenticated request")
    }

    private fun currentRequest(): HttpServletRequest {
        val attributes = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
            ?: throw UnauthorizedException("Missing request context")
        return attributes.request
    }
}
