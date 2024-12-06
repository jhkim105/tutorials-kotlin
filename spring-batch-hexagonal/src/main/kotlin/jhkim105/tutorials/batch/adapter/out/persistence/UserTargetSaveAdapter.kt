package jhkim105.tutorials.batch.adapter.out.persistence

import jhkim105.tutorials.batch.application.domain.model.User
import jhkim105.tutorials.batch.application.port.out.UserSavePort

//@Component
class UserTargetSaveAdapter(
    private val userTargetJpaRepository: UserTargetJpaRepository
) : UserSavePort {

    override fun saveAll(users: List<User>) {
        val targetUserEntities = users.map { user ->
            UserTargetJpaEntity(
                user.id,
                user.username,
            )
        }
        userTargetJpaRepository.saveAll(targetUserEntities)
    }

}