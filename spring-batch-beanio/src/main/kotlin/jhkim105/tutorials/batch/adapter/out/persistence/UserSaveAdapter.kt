package jhkim105.tutorials.batch.adapter.out.persistence

import jakarta.transaction.Transactional
import jhkim105.tutorials.batch.application.domain.entity.User
import jhkim105.tutorials.batch.application.port.out.UserSavePort
import org.springframework.stereotype.Component

@Component
class UserSaveAdapter(
    private val userJpaRepository: UserJpaRepository
) : UserSavePort {

    @Transactional
    override fun saveAll(users: List<User>) {
        val entities = users.map {
            UserJpaEntity(
                id = it.id,
                username = it.username,
                name = it.name
            )
        }
        userJpaRepository.saveAll(entities)
    }

}