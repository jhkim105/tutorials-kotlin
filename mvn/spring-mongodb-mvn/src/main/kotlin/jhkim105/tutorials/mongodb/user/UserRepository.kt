package jhkim105.tutorials.mongodb.user

import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

interface UserRepository : MongoRepository<User, String>{

    fun findByUsername(username: String): User?
}

interface UsernameOnly {
    fun getUsername(): String
}

@Repository
class UserQueryDslRepository(
    private val mongoTemplate: MongoTemplate,
) {

//    fun findByUsername(username: String): User? {
////        val user = QUser.user
//    }
}