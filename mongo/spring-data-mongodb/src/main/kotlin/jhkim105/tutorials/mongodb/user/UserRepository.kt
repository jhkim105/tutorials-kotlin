package jhkim105.tutorials.mongodb.user

import org.springframework.data.mongodb.repository.MongoRepository

interface UserRepository : MongoRepository<User, String>{

    fun findByUsername(username: String): User?
}

interface UsernameOnly {
    fun getUsername(): String
}