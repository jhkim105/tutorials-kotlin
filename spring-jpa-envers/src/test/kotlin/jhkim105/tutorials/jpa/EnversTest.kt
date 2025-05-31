package jhkim105.tutorials.jpa

import jhkim105.tutorials.jpa.model.Group
import jhkim105.tutorials.jpa.model.User
import jhkim105.tutorials.jpa.repository.GroupRepository
import jhkim105.tutorials.jpa.repository.UserRepository
import jhkim105.tutorials.jpa.service.AuditService
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.Rollback
import org.springframework.transaction.annotation.Transactional
import java.util.function.Consumer
import kotlin.test.Test

@SpringBootTest
class EnversTest @Autowired constructor(
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository,
    private val auditService: AuditService
)  {

    @Test
    fun test() {
        // 그룹 생성
        val group = Group(name = "개발팀")

        val user1 = User(username = "alice")
        val user2 = User(username = "bob")

        group.addUser(user1)
        group.addUser(user2)

        val savedGroup = groupRepository.save(group)

        // 사용자 수정
        user1.username = "alice-updated"
        userRepository.save(user1)

        // 사용자 제거
        savedGroup.removeUser(user2)
        groupRepository.save(savedGroup)

        // 이력 조회
        println("=== Group 변경 이력 ===")
        val groupHistories = auditService.getGroupRevisions(savedGroup.id!!)
        groupHistories.forEach {
            println("- 이름: ${it.name}, 사용자 수: ${it.users.size}")
        }

        // Group 검증
        assertThat(groupHistories).hasSize(2)
        assertThat(groupHistories[0].name).isEqualTo("개발팀")
        assertThat(groupHistories[0].users.size).isEqualTo(2)
        assertThat(groupHistories[1].users.size).isEqualTo(1)

        println("=== User 변경 이력 ===")
        val userHistories = auditService.getUserRevisions(user1.id!!)
        userHistories.forEach {
            val groupName = it.group?.name ?: "없음"
            println("- 이름: ${it.username}, 소속 그룹: $groupName")
        }

        // User 검증
        assertThat(userHistories).hasSize(2)
        assertThat(userHistories[0].username).isEqualTo("alice")
        assertThat(userHistories[1].username).isEqualTo("alice-updated")
        assertThat(userHistories[1].group?.name).isEqualTo("개발팀")
    }
}