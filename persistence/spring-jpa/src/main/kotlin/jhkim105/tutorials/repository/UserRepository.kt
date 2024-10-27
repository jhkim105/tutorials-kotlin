package jhkim105.tutorials.repository

import jhkim105.tutorials.domain.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, String>, UserRepositoryCustom {
    fun findByUsername(username: String): User?
}