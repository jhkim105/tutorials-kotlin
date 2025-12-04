package jhkim105.tutorials.security

import jhkim105.tutorials.user.Role
import jhkim105.tutorials.user.User

data class UserPrincipal(val id: String, val authority: String) {

    companion object {
        const val AUTHORITY_SEPARATOR = ","
    }

    constructor(user: User) : this(
        user.id ?: "",
        user.roles.joinToString(AUTHORITY_SEPARATOR) { role: Role -> role.name },
    )
}
