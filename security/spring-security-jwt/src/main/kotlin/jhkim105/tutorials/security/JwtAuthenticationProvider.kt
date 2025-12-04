package jhkim105.tutorials.security

import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.GrantedAuthority

class JwtAuthenticationProvider : AuthenticationProvider {

    override fun authenticate(authentication: Authentication): Authentication? {
        val userPrincipal = authentication.principal as? UserPrincipal ?: return null
        return JwtAuthenticationToken(userPrincipal, authorities(userPrincipal))
    }

    private fun authorities(userPrincipal: UserPrincipal): Set<GrantedAuthority> =
        userPrincipal.authority
            .split(UserPrincipal.AUTHORITY_SEPARATOR)
            .filter { it.isNotBlank() }
            .map { authority -> GrantedAuthority { authority } }
            .toSet()

    override fun supports(authentication: Class<*>): Boolean =
        JwtAuthenticationToken::class.java == authentication
}
