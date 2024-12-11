package jhkim105.tutorials.user.repository

import jhkim105.tutorials.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserJpaRepository : JpaRepository<User, String> {
    fun findByUsername(username: String): User?
}