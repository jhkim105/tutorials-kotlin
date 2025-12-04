package jhkim105.tutorials.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler

class TokenAuthenticationSuccessHandler : SimpleUrlAuthenticationSuccessHandler() {

    private val logger = LoggerFactory.getLogger(TokenAuthenticationSuccessHandler::class.java)

    override fun onAuthenticationSuccess(request: HttpServletRequest, response: HttpServletResponse, authentication: Authentication) {
        logger.debug("onAuthenticationSuccess")
    }
}
