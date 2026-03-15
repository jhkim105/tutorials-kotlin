package jhkim105.tutorials.user.service

import jakarta.transaction.Transactional
import jhkim105.tutorials.user.repository.UserRepository
import org.springframework.stereotype.Service


@Service
class UserService(
    private val userRepository: UserRepository
) {

    @Transactional
    fun deleteAll() {
        userRepository.deleteAll()
        Thread.sleep(1000 * 30)
    }


}