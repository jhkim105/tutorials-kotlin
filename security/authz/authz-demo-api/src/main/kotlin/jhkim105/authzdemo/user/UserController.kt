package jhkim105.authzdemo.user

import jhkim105.authzdemo.auth.AUTH_USER_ATTRIBUTE
import jhkim105.authzdemo.auth.AllowRoles
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService
) {

    @GetMapping
    @AllowRoles(Role.USER, Role.ADMIN)
    fun listUsers(
        @RequestAttribute(name = AUTH_USER_ATTRIBUTE) actor: User
    ): List<UserResponse> =
        userService.getUsers(actor).map { it.toResponse() }

    @GetMapping("/{id}")
    @AllowRoles(Role.USER, Role.ADMIN)
    fun getUser(
        @PathVariable id: String,
        @RequestAttribute(name = AUTH_USER_ATTRIBUTE) actor: User
    ): UserResponse =
        userService.getUser(id, actor).toResponse()

    @PostMapping
    @AllowRoles(Role.ADMIN)
    fun createUser(
        @RequestBody request: CreateUserRequest,
        @RequestAttribute(name = AUTH_USER_ATTRIBUTE) actor: User
    ): UserResponse =
        userService.createUser(request, actor).toResponse()

    @PutMapping("/{id}")
    @AllowRoles(Role.ADMIN)
    fun updateUser(
        @PathVariable id: String,
        @RequestBody request: UpdateUserRequest,
        @RequestAttribute(name = AUTH_USER_ATTRIBUTE) actor: User
    ): UserResponse =
        userService.updateUser(id, request, actor).toResponse()
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
