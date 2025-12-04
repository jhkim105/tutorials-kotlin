package jhkim105.tutorials.security

import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContextHolder

object SecurityUtils {

    fun getAuthUser(): UserPrincipal {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw AccessDeniedException("Authentication not exists.")
        val principal = authentication.principal
        if (principal is UserPrincipal) {
            return principal
        }
        throw AccessDeniedException("Not properly authenticated.")
    }

    fun getAuthUserSilently(): UserPrincipal? = try {
        getAuthUser()
    } catch (_: AccessDeniedException) {
        null
    }
}
