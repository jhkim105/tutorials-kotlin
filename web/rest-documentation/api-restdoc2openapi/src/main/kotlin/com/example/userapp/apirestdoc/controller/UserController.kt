package com.example.userapp.apirestdoc.controller

import com.example.userapp.core.application.port.`in`.CreateUserUseCase
import com.example.userapp.core.application.port.`in`.GetUserUseCase
import com.example.userapp.core.application.port.`in`.DeleteUserUseCase
import com.example.userapp.core.domain.User
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
@Validated
class UserController(
    private val createUser: CreateUserUseCase,
    private val getUser: GetUserUseCase,
    private val deleteUser: DeleteUserUseCase
) {

    data class CreateUserRequest(
        @field:NotBlank val name: String,
        @field:Email val email: String
    )
    data class UserResponse(
        val id: Long?, val name: String, val email: String
    ) {
        companion object {
            fun from(u: User) = UserResponse(u.id?.value, u.name, u.email)
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody req: CreateUserRequest): UserResponse {
        val created = createUser.create(CreateUserUseCase.CreateUserCommand(req.name, req.email))
        return UserResponse.from(created)
    }

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): UserResponse? =
        getUser.getById(id)?.let { UserResponse.from(it) }

    @GetMapping
    fun list(): List<UserResponse> =
        getUser.list().map { UserResponse.from(it) }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) = deleteUser.delete(id)
}
