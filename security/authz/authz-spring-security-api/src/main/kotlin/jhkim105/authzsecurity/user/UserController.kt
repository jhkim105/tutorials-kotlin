package jhkim105.authzsecurity.user

import jhkim105.authzsecurity.security.UserPrincipal
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService
) {

    @GetMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    fun listUsers(
        @AuthenticationPrincipal actor: UserPrincipal
    ): List<UserResponse> =
        userService.getUsers(actor.user).map { it.toResponse() }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    fun getUser(
        @PathVariable id: String,
        @AuthenticationPrincipal actor: UserPrincipal
    ): UserResponse =
        userService.getUser(id, actor.user).toResponse()

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun createUser(
        @RequestBody request: CreateUserRequest,
        @AuthenticationPrincipal actor: UserPrincipal
    ): UserResponse =
        userService.createUser(request, actor.user).toResponse()

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateUser(
        @PathVariable id: String,
        @RequestBody request: UpdateUserRequest,
        @AuthenticationPrincipal actor: UserPrincipal
    ): UserResponse =
        userService.updateUser(id, request, actor.user).toResponse()
}

data class UserResponse(
    val id: String,
    val name: String,
    val role: Role
)

private fun User.toResponse() = UserResponse(
    id = id,
    name = name,
    role = role
)
