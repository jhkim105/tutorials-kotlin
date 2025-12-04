package jhkim105.authzsecurity.user

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class InMemoryUserRepository : UserRepository {
    private val users = ConcurrentHashMap<String, User>()

    init {
        users["admin"] = User(id = "admin", name = "Administrator", role = Role.ADMIN)
        users["alice"] = User(id = "alice", name = "Alice", role = Role.USER)
        users["bob"] = User(id = "bob", name = "Bob", role = Role.USER)
    }

    override fun findById(id: String): User? = users[id]

    override fun findAll(): List<User> = users.values.sortedBy { it.id }

    override fun save(user: User): User {
        users[user.id] = user
        return user
    }
}
