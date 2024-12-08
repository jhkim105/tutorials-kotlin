package jhkim105.tutorials.batch.application.domain.service

import jhkim105.tutorials.batch.application.domain.entity.User
import jhkim105.tutorials.batch.application.port.`in`.UserSaveUseCase
import jhkim105.tutorials.batch.application.port.out.UserSavePort
import org.springframework.stereotype.Service

@Service
class UserSaveService(
    private val userSavePort: UserSavePort
) : UserSaveUseCase {
    override fun saveAll(users: List<User>) {
        userSavePort.saveAll(users = users)
    }
}