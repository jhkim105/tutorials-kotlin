package jhkim105.tutorials.jpa

import jakarta.persistence.EntityManager
import jhkim105.tutorials.jpa.model.User
import jhkim105.tutorials.jpa.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.hibernate.envers.AuditReaderFactory
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@TestMethodOrder(OrderAnnotation::class)
class UserHistoryTest @Autowired constructor(
    val userRepository: UserRepository
) {

    companion object {
        var userId: Long? = null
    }

    @Test
    @Order(1)
    fun `create user 엔버스 히스토리 생성 확인`() {
        val user = User(username = "alice")
        val saved = userRepository.save(user)
        userId = saved.id

        val revisions = userRepository.findRevisions(userId!!)
        assertThat(revisions).hasSize(1)
        val revision = userRepository.findLastChangeRevision(userId!!).orElseThrow()
        assertThat(revision?.entity?.username).isEqualTo("alice")
    }

    @Test
    @Order(2)
    fun `update user 엔버스 히스토리 추가 확인`() {
        val user = userRepository.findById(userId!!).orElseThrow()
        user.username = "alice-updated"
        userRepository.save(user)

        val revisions = userRepository.findRevisions(userId!!)
        assertThat(revisions).hasSize(2)
        val revision = userRepository.findLastChangeRevision(userId!!).orElseThrow()
        assertThat(revision?.entity?.username).isEqualTo("alice-updated")
    }

    @Test
    @Order(3)
    fun `delete user 엔버스 히스토리 남아있는지 확인`() {
        val user = userRepository.findById(userId!!).orElseThrow()
        userRepository.delete(user)

        val revisions = userRepository.findRevisions(userId!!)
        assertThat(revisions).hasSize(3)
        val revision = userRepository.findLastChangeRevision(userId!!).orElseThrow()
        assertThat(revision?.entity?.username).isEqualTo("alice-updated")
    }

    @Test
    @Order(4)
    fun `히스토리 이력을 조회해 변경 내용 확인`() {
        val revisions = userRepository.findRevisions(userId!!)
        val history = revisions.map { rev ->
            val revision = userRepository.findRevision(userId!!, rev.revisionNumber.get()).orElseThrow()
            revision.entity.username
        }

        assertThat(history).containsExactly("alice", "alice-updated", "alice-updated")
    }


}