package jhkim105.authzdemo.user

interface UserRepository {
    fun findById(id: String): User?
    fun findAll(): List<User>
    fun save(user: User): User
}
