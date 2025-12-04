package jhkim105.tutorials.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint

class TokenAuthenticationEntryPoint(
    private val securityErrorHandler: SecurityErrorHandler,
) : AuthenticationEntryPoint {

    private val logger = LoggerFactory.getLogger(TokenAuthenticationEntryPoint::class.java)

    override fun commence(request: HttpServletRequest, response: HttpServletResponse, authException: AuthenticationException) {
        logger.debug("TokenAuthenticationEntryPoint::path:{}, message:{}", request.requestURI, authException.message)
        securityErrorHandler.handleUnauthorized(response)
    }
}
