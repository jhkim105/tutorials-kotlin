package com.example.userapp.core.application.port.`in`

import com.example.userapp.core.domain.User
import com.example.userapp.core.domain.UserId

interface CreateUserUseCase {
    fun create(command: CreateUserCommand): User

    data class CreateUserCommand(
        val name: String,
        val email: String
    )
}

interface GetUserUseCase {
    fun getById(id: Long): User?
    fun list(): List<User>
}

interface DeleteUserUseCase {
    fun delete(id: Long)
}
