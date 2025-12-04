package jhkim105.tutorials.security

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority

class JwtAuthenticationToken(
    private val principalValue: Any?,
    authorities: Collection<GrantedAuthority>? = null,
    authenticated: Boolean = false,
) : AbstractAuthenticationToken(authorities) {

    init {
        isAuthenticated = authenticated
    }

    constructor(principal: Any?) : this(principal, null, false)

    constructor(principal: Any?, authorities: Collection<GrantedAuthority>) : this(principal, authorities, true)

    override fun getCredentials(): Any? = null

    override fun getPrincipal(): Any? = principalValue
}
