
## Dependency
```text
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-validation") // ✨ Validation 의존성

```

## Validation 적용
#### Dto
```kotlin
data class UserRequest(
    @field:NotBlank(message = "이름은 필수입니다.")
    val name: String,

    @field:Email(message = "이메일 형식이 올바르지 않습니다.")
    val email: String,

    @field:Size(min = 6, message = "비밀번호는 최소 6자 이상이어야 합니다.")
    val password: String
)

```

### Controller
@Valid
```kotlin
@RestController
@RequestMapping("/users")
class UserController {

    @PostMapping
    fun createUser(@Valid @RequestBody request: UserRequest): ResponseEntity<String> {
        // 유효성 검사를 통과한 경우
        return ResponseEntity.ok("사용자 생성 완료: ${request.name}")
    }
}
```

## @PathVariable, @RequestParam Validation
@Validated 와 Path Variable Validation
### Controller
```kotlin
@RestController
@RequestMapping("/users")
@Validated
class UserController {
    
    @GetMapping("/{id}")
    fun getUserById(@PathVariable @Min(1, message = "ID는 1 이상이어야 합니다.") id: Long): String {
        return "ID $id 유저 조회"
    }
}
```

## Custom Validator

### Annotation

```kotlin
import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@MustBeDocumented
@Constraint(validatedBy = [PasswordValidator::class])
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ValidPassword(
    val message: String = "비밀번호는 숫자를 포함해야 합니다.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
```

###  Validator
```kotlin
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class PasswordValidator : ConstraintValidator<ValidPassword, String> {
    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        if (value == null) return false
        return value.any { it.isDigit() }
    }
}

```

### Dto 적용
```kotlin
data class UserRequest(

    @field:ValidPassword // Custom Validator 적용
    val password: String
)
```