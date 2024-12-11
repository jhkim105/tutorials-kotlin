package jhkim105.tutorials.user.repository

import jhkim105.tutorials.user.entity.User
import org.springframework.stereotype.Repository

@Repository
class UserRepository(
    private val userJpaRepository: UserJpaRepository,
    private val userQueryDslRepository: UserQueryDslRepository

) {

    fun findAllByCompanyName(companyName: String): MutableList<User> {
        return userQueryDslRepository.findAllByCompanyName(companyName)
    }

    fun deleteAll() {
        userJpaRepository.deleteAll()
    }
}