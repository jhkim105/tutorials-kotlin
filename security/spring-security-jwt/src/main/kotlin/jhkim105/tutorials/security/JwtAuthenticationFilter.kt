package jhkim105.tutorials.security

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jhkim105.tutorials.jwt.JwtService
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.InternalAuthenticationServiceException
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter
import java.io.IOException

class JwtAuthenticationFilter(
    defaultFilterProcessesUrl: String,
    private val tokenService: JwtService,
    securityErrorHandler: SecurityErrorHandler,
) : AbstractAuthenticationProcessingFilter(defaultFilterProcessesUrl) {

    private val log = KotlinLogging.logger {}

    init {
        setAuthenticationSuccessHandler(TokenAuthenticationSuccessHandler())
        setAuthenticationFailureHandler(TokenAuthenticationFailureHandler(securityErrorHandler))
    }

    @Throws(AuthenticationException::class)
    override fun attemptAuthentication(request: HttpServletRequest, response: HttpServletResponse): Authentication? {
        log.debug { "attemptAuthentication" }
        val token = getToken(request)
        if (token.isNullOrBlank()) {
            return null
        }

        return try {
            val userPrincipal = tokenService.parseToken(token!!)
            val jwtAuthenticationToken = JwtAuthenticationToken(userPrincipal)
            authenticationManager.authenticate(jwtAuthenticationToken)
        } catch (e: RuntimeException) {
            throw InternalAuthenticationServiceException(e.toString(), e)
        }
    }

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(req: ServletRequest, res: ServletResponse, chain: FilterChain) {
        log.debug { "doFilter" }
        val request = req as HttpServletRequest
        val response = res as HttpServletResponse
        val token = getToken(request)
        if (token.isNullOrBlank()) {
            chain.doFilter(request, response)
            return
        }
        super.doFilter(req, res, chain)
    }

    @Throws(IOException::class, ServletException::class)
    override fun successfulAuthentication(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain, authResult: Authentication) {
        super.successfulAuthentication(request, response, chain, authResult)
        chain.doFilter(request, response)
    }

    private fun getToken(request: HttpServletRequest): String? = request.getHeader(HttpHeaders.AUTHORIZATION)
}
