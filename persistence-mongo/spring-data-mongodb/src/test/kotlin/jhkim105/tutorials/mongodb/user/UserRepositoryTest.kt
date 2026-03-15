package jhkim105.tutorials.mongodb.user

import io.kotest.matchers.equals.shouldBeEqual
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.test.context.ActiveProfiles
import kotlin.test.Test

@DataMongoTest
@ActiveProfiles("cluster")
class UserRepositoryTest(
    @Autowired val userRepository: UserRepository,
) {

    @BeforeEach
    fun beforeEach() {
        userRepository.deleteAll()
    }

    @Test
    fun test() {
        val user = User(username= "user01", email = EmailAddress("test@test.com"))
        userRepository.save(user)
        val savedUser = userRepository.findByUsername("user01")!!
        savedUser.username shouldBeEqual user.username
    }
}