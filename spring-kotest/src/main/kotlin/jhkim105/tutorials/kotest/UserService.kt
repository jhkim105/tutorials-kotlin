package jhkim105.tutorials.kotest

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class UserService(private val userRepository: UserRepository) {

    fun getAllUsers(): List<User> = userRepository.findAll()

    fun getUserById(id: Long): User? = userRepository.findById(id).orElse(null)

    fun createUser(user: User): User = userRepository.save(user)

    fun updateUser(id: Long, updatedUser: User): User? {
        return userRepository.findByIdOrNull(id)?.apply {
            name = updatedUser.name
            email = updatedUser.email
        }
    }

    fun deleteUser(id: Long) {
        userRepository.deleteById(id)
    }
}