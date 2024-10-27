package jhkim105.tutorials.service

import jakarta.transaction.Transactional
import jhkim105.tutorials.repository.UserRepository
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