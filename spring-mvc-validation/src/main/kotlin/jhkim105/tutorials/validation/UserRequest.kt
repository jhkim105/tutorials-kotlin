package jhkim105.tutorials.validation

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import jhkim105.tutorials.validation.custom.ValidPassword

data class UserRequest(
    @field:NotBlank(message = "이름은 필수입니다.")
    val name: String,

    @field:Email(message = "이메일 형식이 올바르지 않습니다.")
    val email: String,

    @field:Size(min = 6, message = "비밀번호는 최소 6자 이상이어야 합니다.")
    @field:ValidPassword
    val password: String

)
