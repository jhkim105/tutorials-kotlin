package jhkim105.tutorials.redis.user

import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest
import org.springframework.data.repository.findByIdOrNull
import kotlin.test.Test


@DataRedisTest
class UserRepositoryTest(
    @Autowired val repository: UserRepository
) {

    @Test
    fun save() {
        val user = User(
            username = "user01",
            role = Role.USER,
            ttl = 60L
        )

        repository.save(user)

        val savedUser = repository.findByIdOrNull(user.id!!)
        assertThat(savedUser?.id).isEqualTo(user.id)
    }
}