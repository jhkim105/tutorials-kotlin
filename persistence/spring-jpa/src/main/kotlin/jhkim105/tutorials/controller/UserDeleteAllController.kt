package jhkim105.tutorials.controller

import jhkim105.tutorials.repository.UserRepository
import jhkim105.tutorials.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class UserDeleteAllController(private val userService: UserService) {

    @DeleteMapping("/users/all")
    fun deleteAll(): ResponseEntity<Void> {
        userService.deleteAll()
        return ResponseEntity.ok().build();
    }

}


