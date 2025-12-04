package jhkim105.authzsecurity.user

import jhkim105.authzsecurity.common.NotFoundException
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository
) {

    fun getUsers(actor: User): List<User> {
        return userRepository.findAll()
    }

    fun getUser(id: String, actor: User): User {
        return userRepository.findById(id) ?: throw NotFoundException("User $id not found")
    }

    fun createUser(request: CreateUserRequest, actor: User): User {
        if (userRepository.findById(request.id) != null) {
            throw IllegalArgumentException("User ${request.id} already exists")
        }
        val newUser = User(id = request.id, name = request.name, role = request.role)
        return userRepository.save(newUser)
    }

    fun updateUser(id: String, request: UpdateUserRequest, actor: User): User {
        val existing = userRepository.findById(id) ?: throw NotFoundException("User $id not found")
        val updated = existing.copy(
            name = request.name ?: existing.name,
            role = request.role ?: existing.role
        )
        return userRepository.save(updated)
    }
}

data class CreateUserRequest(
    val id: String,
    val name: String,
    val role: Role
)

data class UpdateUserRequest(
    val name: String? = null,
    val role: Role? = null
)
