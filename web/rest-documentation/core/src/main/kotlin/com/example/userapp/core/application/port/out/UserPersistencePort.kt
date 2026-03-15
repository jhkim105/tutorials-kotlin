package com.example.userapp.core.application.port.out

import com.example.userapp.core.domain.User
import com.example.userapp.core.domain.UserId

interface UserPersistencePort {
    fun save(user: User): User
    fun findById(id: Long): User?
    fun findAll(): List<User>
    fun deleteById(id: Long)
    fun existsByEmail(email: String): Boolean
}
