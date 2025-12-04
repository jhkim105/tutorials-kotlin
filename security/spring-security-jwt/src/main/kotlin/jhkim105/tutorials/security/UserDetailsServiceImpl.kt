package jhkim105.tutorials.security

import jhkim105.tutorials.user.UserRepository
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.transaction.annotation.Transactional

open class UserDetailsServiceImpl(
    private val userRepository: UserRepository,
) : UserDetailsService {

    @Transactional(readOnly = true)
    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("User not found: $username")
        return object : UserDetails {
            override fun getAuthorities(): Collection<GrantedAuthority> =
                user.roles.map { role -> GrantedAuthority { role.name } }

            override fun getPassword(): String = user.password ?: ""

            override fun getUsername(): String = user.username ?: ""
        }
    }
}
