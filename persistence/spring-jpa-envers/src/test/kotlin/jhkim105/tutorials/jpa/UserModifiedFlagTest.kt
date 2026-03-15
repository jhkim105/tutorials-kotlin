package jhkim105.tutorials.jpa

import jhkim105.tutorials.jpa.model.User
import jhkim105.tutorials.jpa.repository.UserRepository
import jhkim105.tutorials.jpa.service.RevisionQueryService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@TestMethodOrder(OrderAnnotation::class)
class UserModifiedFlagTest @Autowired constructor(
    val userRepository: UserRepository,
    val revisionService: RevisionQueryService
) {
    private var userId: Long? = null

    @Test
    fun `test modified flag`() {
        val user = User(username = "alice")
        val saved = userRepository.save(user)
        userId = saved.id

        saved.username = "updated_alice"
        userRepository.save(user)

        // 3. 최신 리비전 번호 조회
        val latestRev = revisionService.getLatestRevisionNumber(User::class.java, userId!!)

        // 4. 필드 변경 여부 확인
        val usernameChanged = revisionService.isModified(User::class.java, userId!!, "username", latestRev)
        val emailChanged = revisionService.isModified(User::class.java, userId!!, "email", latestRev)

        // 5. 검증
        assertThat(usernameChanged).isTrue()
        assertThat(emailChanged).isFalse()
    }

}