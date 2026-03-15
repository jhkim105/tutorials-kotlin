package jhkim105.tutorials.validation

import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid
import jakarta.validation.constraints.Min

@RestController
@RequestMapping("/users")
@Validated
class UserController {

    @PostMapping
    fun createUser(@Valid @RequestBody request: UserRequest): ResponseEntity<String> {
        return ResponseEntity.ok("Success: ${request.name}")
    }

    @GetMapping("/{id}")
    fun getUserById(@PathVariable @Min(1, message = "ID는 1 이상이어야 합니다.") id: Long): String {
        return "ID $id 유저 조회"
    }
}
