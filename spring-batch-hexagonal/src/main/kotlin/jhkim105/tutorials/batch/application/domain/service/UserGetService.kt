package jhkim105.tutorials.batch.application.domain.service

import jhkim105.tutorials.batch.application.domain.model.User
import jhkim105.tutorials.batch.application.port.`in`.UserGetUseCase
import jhkim105.tutorials.batch.application.port.out.UserGetPort
import org.springframework.stereotype.Service

@Service
class UserGetService(
    private val userGetPort: UserGetPort
) : UserGetUseCase {
    override fun getUsers(): List<User> {
        return userGetPort.getUsers()
    }
}