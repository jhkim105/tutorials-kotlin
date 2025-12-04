package jhkim105.tutorials.user

import jhkim105.tutorials.security.UserPrincipal
import org.slf4j.LoggerFactory
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService,
) {

    private val logger = LoggerFactory.getLogger(UserController::class.java)

    @GetMapping("/me")
    fun me(@AuthenticationPrincipal userPrincipal: UserPrincipal): UserPrincipal = userPrincipal

    @PostMapping
    fun save(@RequestBody userUpdateRequest: UserUpdateRequest): User {
        logger.debug("user update request {}", userUpdateRequest)
        val currentUser = userService.getCurrentUser()
        userUpdateRequest.applyTo(currentUser)
        return userService.save(currentUser)
    }
}
