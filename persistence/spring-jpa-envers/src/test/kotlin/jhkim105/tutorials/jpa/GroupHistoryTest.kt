package jhkim105.tutorials.jpa

import jhkim105.tutorials.jpa.model.Group
import jhkim105.tutorials.jpa.model.User
import jhkim105.tutorials.jpa.repository.GroupRepository
import jhkim105.tutorials.jpa.service.GroupService
import jhkim105.tutorials.jpa.service.UserService
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.Test

@SpringBootTest
class GroupHistoryTest @Autowired constructor(
    private val groupRepository: GroupRepository,
    private val groupService: GroupService,
    private val userService: UserService
)  {

    @Test
    fun createGroup() {
        // 그룹 생성
        val group = Group(name = "개발팀")

        val user1 = User(username = "alice")
        val user2 = User(username = "bob")

        group.addUser(user1)
        group.addUser(user2)

        val savedGroup = groupRepository.save(group)

        // 이력 조회
        println("=== Group 변경 이력 ===")
        val groupHistories = groupService.getGroupRevisions(savedGroup.id!!)
        groupHistories.forEach {
            println("- 이름: ${it.name}, 사용자 수: ${it.users.size}")
        }

        // Group 검증
        assertThat(groupHistories).hasSize(1)
        assertThat(groupHistories[0].name).isEqualTo("개발팀")
        assertThat(groupHistories[0].users.size).isEqualTo(2)

        println("=== User 변경 이력 ===")
        val userHistories = userService.getUserRevisions(user1.id!!)
        userHistories.forEach {
            val groupName = it.group?.name ?: "없음"
            println("- 이름: ${it.username}, 소속 그룹: $groupName")
        }

        // User 검증
        assertThat(userHistories).hasSize(1)
        assertThat(userHistories[0].username).isEqualTo("alice")
    }

    @Test
    fun deleteGroupUser() {
        val group = Group(name = "개발팀")
        val user1 = User(username = "alice")
        val user2 = User(username = "bob")
        group.addUser(user1)
        group.addUser(user2)
        val savedGroup = groupRepository.save(group)

        groupRepository.delete(group)

        // 이력 조회
        println("=== Group 변경 이력 ===")
        val groupHistories = groupService.getGroupRevisions(savedGroup.id!!)
        groupHistories.forEach {
            println("- 이름: ${it.name}, 사용자 수: ${it.users.size}")
        }

        // Group 검증
        assertThat(groupHistories).hasSize(2)
        assertThat(groupHistories[0].name).isEqualTo("개발팀")
        assertThat(groupHistories[0].users.size).isEqualTo(2)
        assertThat(groupHistories[1].users.size).isEqualTo(2)

        println("=== User 변경 이력 ===")
        val userHistories = userService.getUserRevisions(user1.id!!)
        userHistories.forEach {
            val groupName = it.group?.name ?: "없음"
            println("- 이름: ${it.username}, 소속 그룹: $groupName")
        }

        // User 검증
        assertThat(userHistories).hasSize(2)
        assertThat(userHistories[0].username).isEqualTo("alice")
        assertThat(userHistories[1].username).isEqualTo("alice")
        assertThat(userHistories[1].group?.name).isEqualTo("개발팀")
    }
}
