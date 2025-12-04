package jhkim105.tutorials.security

import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import java.io.IOException

class TokenAuthenticationFailureHandler(
    private val securityErrorHandler: SecurityErrorHandler,
) : SimpleUrlAuthenticationFailureHandler() {

    private val logger = LoggerFactory.getLogger(TokenAuthenticationFailureHandler::class.java)

    @Throws(IOException::class, ServletException::class)
    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException,
    ) {
        logger.debug("onAuthenticationFailure")
        securityErrorHandler.handleUnauthorized(response)
    }
}
