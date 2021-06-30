package com.example.demo.user

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.stream.Collectors

@RestController
class UserController(private val repository: UserRepository) {

    @GetMapping("/users")
    fun list(pageable: Pageable): ResponseEntity<Page<UserDto>> {
        val page  = repository.findAll(pageable)
        val list = page.stream().map{user -> user.dto()}.collect(Collectors.toList())

        return ResponseEntity.ok(PageImpl(list, pageable, page.totalElements));
    }


    fun User.dto() = UserDto(username, name, description)

    data class UserDto (
        val username: String,
        val name: String,
        val description: String? = null)
}


