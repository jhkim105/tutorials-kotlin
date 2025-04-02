package jhkim105.tutorials.kotest

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest @Autowired constructor(
    private val userRepository: UserRepository
) : FreeSpec({

    "User를 저장하고 조회할 수 있어야 한다" - {
        val user = User(name = "John Doe", email = "john@example.com")
        val savedUser = userRepository.save(user)
        val foundUser = userRepository.findById(savedUser.id).orElse(null)

        foundUser shouldBe savedUser
    }
})