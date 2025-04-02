package jhkim105.tutorials.kotest

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*

class UserServiceTest : StringSpec({

    val userRepository = mockk<UserRepository>()
    val userService = UserService(userRepository)

    "getUserById는 ID로 사용자를 찾는다" {
        val user = User(id = 1, name = "John Doe", email = "john@example.com")

        every { userRepository.findById(1) } returns Optional.of(user)

        val foundUser = userService.getUserById(1)

        foundUser shouldBe user
        verify(exactly = 1) { userRepository.findById(1) }
    }
})