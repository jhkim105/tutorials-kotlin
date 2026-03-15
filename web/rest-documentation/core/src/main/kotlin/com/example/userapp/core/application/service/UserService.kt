package com.example.userapp.core.application.service

import com.example.userapp.core.application.port.`in`.CreateUserUseCase
import com.example.userapp.core.application.port.`in`.DeleteUserUseCase
import com.example.userapp.core.application.port.`in`.GetUserUseCase
import com.example.userapp.core.application.port.out.UserPersistencePort
import com.example.userapp.core.domain.User
import com.example.userapp.core.domain.UserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val persistence: UserPersistencePort
) : CreateUserUseCase, GetUserUseCase, DeleteUserUseCase {

    @Transactional
    override fun create(command: CreateUserUseCase.CreateUserCommand): User {
        if (persistence.existsByEmail(command.email)) {
            throw IllegalArgumentException("Email already exists: ${command.email}")
        }
        val toSave = User(
            id = null,
            name = command.name,
            email = command.email
        )
        return persistence.save(toSave)
    }

    @Transactional(readOnly = true)
    override fun getById(id: Long): User? = persistence.findById(id)

    @Transactional(readOnly = true)
    override fun list(): List<User> = persistence.findAll()

    @Transactional
    override fun delete(id: Long) {
        persistence.deleteById(id)
    }
}
