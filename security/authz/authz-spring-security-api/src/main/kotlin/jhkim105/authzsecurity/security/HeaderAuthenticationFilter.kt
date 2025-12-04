package jhkim105.authzsecurity.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jhkim105.authzsecurity.user.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

const val USER_ID_HEADER = "X-User-Id"

@Component
class HeaderAuthenticationFilter(
    private val userRepository: UserRepository,
    private val errorResponseWriter: ErrorResponseWriter
) : OncePerRequestFilter() {

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return request.servletPath.startsWith("/actuator")
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val existingAuth = SecurityContextHolder.getContext().authentication
            if (existingAuth?.isAuthenticated == true) {
                filterChain.doFilter(request, response)
                return
            }

            val userId = request.getHeader(USER_ID_HEADER)?.takeIf { it.isNotBlank() }
                ?: throw AuthenticationCredentialsNotFoundException("Missing $USER_ID_HEADER header")
            val user = userRepository.findById(userId)
                ?: throw BadCredentialsException("Unknown user id: $userId")

            val principal = UserPrincipal(user)
            val authentication = UsernamePasswordAuthenticationToken(principal, null, principal.authorities).apply {
                details = WebAuthenticationDetailsSource().buildDetails(request)
            }

            SecurityContextHolder.getContext().authentication = authentication
            filterChain.doFilter(request, response)
        } catch (ex: AuthenticationException) {
            SecurityContextHolder.clearContext()
            errorResponseWriter.write(response, HttpStatus.UNAUTHORIZED, ex.message)
        }
    }
}
