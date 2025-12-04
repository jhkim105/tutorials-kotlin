package jhkim105.tutorials.user

import jhkim105.tutorials.security.SecurityUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(private val userRepository: UserRepository) {

    @Transactional(readOnly = true)
    fun getCurrentUser(): User {
        val userId = SecurityUtils.getAuthUser().id
        return userRepository.findById(userId).orElseThrow()
    }

    @Transactional
    fun save(user: User): User = userRepository.save(user)

    fun getByUsername(username: String): User? = userRepository.findByUsername(username)
}
