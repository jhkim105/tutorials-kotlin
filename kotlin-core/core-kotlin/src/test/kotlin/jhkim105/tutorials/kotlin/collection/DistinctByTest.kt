package jhkim105.tutorials.kotlin.collection

import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.Test

class DistinctByTest {
    data class UserDto(
        val id: Int,
        val name: String,
        val email: String
    )

    @Test
    fun `이메일 기준 중복 제거`() {
        // given
        val users = listOf(
            UserDto(1, "Alice", "alice@example.com"),
            UserDto(2, "Bob", "bob@example.com"),
            UserDto(3, "Charlie", "charlie@example.com"),
            UserDto(4, "Alice Clone", "alice@example.com"), // 이메일 중복
            UserDto(5, "Bob 2", "bob@example.com")          // 이메일 중복
        )

        // when
        val distinctUsers = users.distinctBy { it.email }

        // then
        assertEquals(3, distinctUsers.size)
        assertEquals(listOf("Alice", "Bob", "Charlie"), distinctUsers.map { it.name })
    }

    @Test
    fun `(name, email) 기준 중복 제거`() {
        // given
        val users = listOf(
            UserDto(1, "Alice", "alice@example.com"),
            UserDto(2, "Bob", "bob@example.com"),
            UserDto(3, "Alice", "alice@example.com"),   // name + email 중복
            UserDto(4, "Alice", "alice@company.com"),   // email 다름 → 다른 사람
            UserDto(5, "Bob", "bob@example.com")        // name + email 중복
        )

        // when
        val distinctUsers = users.distinctBy { it.name to it.email }

        // then
        assertEquals(3, distinctUsers.size)
        assertEquals(
            listOf(
                "Alice" to "alice@example.com",
                "Bob" to "bob@example.com",
                "Alice" to "alice@company.com"
            ),
            distinctUsers.map { it.name to it.email }
        )
    }
}