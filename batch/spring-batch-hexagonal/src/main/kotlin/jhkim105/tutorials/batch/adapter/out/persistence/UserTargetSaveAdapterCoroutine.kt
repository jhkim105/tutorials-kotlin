package jhkim105.tutorials.batch.adapter.out.persistence

import jhkim105.tutorials.batch.application.domain.entity.User
import jhkim105.tutorials.batch.application.port.out.UserSavePort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component

@Component
class UserTargetSaveAdapterCoroutine(
    private val userTargetJpaRepository: UserTargetJpaRepository
) : UserSavePort {

    override fun saveAll(users: List<User>) = runBlocking {
        users.forEach { user ->
            saveAsync(user)
        }
    }

    private fun saveAsync(user: User) = runBlocking {
        launch(Dispatchers.IO) {
            userTargetJpaRepository.save(
                UserTargetJpaEntity(
                    user.id,
                    user.username,
                )
            )
        }
    }

}