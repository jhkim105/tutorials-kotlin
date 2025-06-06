package jhkim105.tutorials.validation

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.get

@WebMvcTest(UserController::class)
class UserControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `사용자 생성 - 성공`() {
        val request = mapOf(
            "name" to "홍길동",
            "email" to "hong@example.com",
            "password" to "pass1234"
        )

        mockMvc.post("/users") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isOk() }
            content { string("Success: 홍길동") }
        }
    }

    @Test
    fun `사용자 생성 - 실패 (검증 오류)`() {
        val request = mapOf(
            "name" to "",
            "email" to "not-an-email",
            "password" to "123"
        )

        mockMvc.post("/users") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.name") { value("이름은 필수입니다.") }
            jsonPath("$.email") { value("이메일 형식이 올바르지 않습니다.") }
            jsonPath("$.password") { value("비밀번호는 최소 6자 이상이어야 합니다.") }
        }
    }

    @Test
    fun `사용자 생성 - 실패 (비밀번호 숫자 없음)`() {
        val request = mapOf(
            "name" to "홍길동",
            "email" to "hong@example.com",
            "password" to "abcdef"  // 숫자 없음 → 실패
        )

        mockMvc.post("/users") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.password") { value("비밀번호는 숫자를 포함해야 합니다.") }
        }
    }

    @Test
    fun `사용자 조회 - 실패 (ID 음수)`() {
        mockMvc.get("/users/0")
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.id") { value("ID는 1 이상이어야 합니다.") }
            }
    }
}