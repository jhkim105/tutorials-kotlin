package jhkim105.tutorials.batch.adapter.out.persistence

import jhkim105.tutorials.batch.application.domain.model.User
import jhkim105.tutorials.batch.application.port.out.UserGetPort
import org.springframework.stereotype.Repository

@Repository
class UserGetAdapter(
    private val userJpaRepository: UserJpaRepository
) : UserGetPort {
    override fun getUsers(): List<User> {
        return userJpaRepository.findAll().map {
            User(
                id = it.id!!,
                username = it.username,
            )
        }
    }
}