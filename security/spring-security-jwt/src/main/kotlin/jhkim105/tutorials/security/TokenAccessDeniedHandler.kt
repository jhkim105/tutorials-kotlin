package jhkim105.tutorials.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler

class TokenAccessDeniedHandler(
    private val securityErrorHandler: SecurityErrorHandler,
) : AccessDeniedHandler {

    override fun handle(request: HttpServletRequest, response: HttpServletResponse, accessDeniedException: AccessDeniedException) {
        securityErrorHandler.handleAccessDenied(response)
    }
}
