package jhkim105.tutorials.kotest

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.core.spec.style.StringSpec
import io.mockk.mockk
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.*
import kotlin.test.Test

@WebMvcTest(UserController::class)
class UserControllerTest @Autowired constructor(
    private val mockMvc: MockMvc
) {
    @MockkBean
    private lateinit var userService: UserService

    private val objectMapper = jacksonObjectMapper()

    @Test
    fun `사용자 조회`() {
        val user = User(id = 1, name = "John Doe", email = "john@example.com")
        every { userService.getUserById(1) } returns user

        mockMvc.get("/users/1")
            .andExpect {
                status { isOk() }
                jsonPath("$.name") { value("John Doe") }
                jsonPath("$.email") { value("john@example.com") }
            }

        verify(exactly = 1) { userService.getUserById(1) }
    }

    @Test
    fun `사용자 생성`() {
        val user = User(id = 1, name = "John Doe", email = "john@example.com")
        every { userService.createUser(any()) } returns user

        mockMvc.post("/users") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(user)
        }.andExpect {
            status { isOk() }
            jsonPath("$.name") { value("John Doe") }
            jsonPath("$.email") { value("john@example.com") }
        }

        verify(exactly = 1) { userService.createUser(any()) }
    }
}