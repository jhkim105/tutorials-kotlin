package com.example.userapp.core.infra.persistence

import com.example.userapp.core.application.port.out.UserPersistencePort
import com.example.userapp.core.domain.User
import com.example.userapp.core.domain.UserId
import com.example.userapp.core.infra.persistence.jpa.UserJpaEntity
import com.example.userapp.core.infra.persistence.jpa.UserJpaRepository
import org.springframework.stereotype.Component

@Component
class UserPersistenceAdapter(
    private val repo: UserJpaRepository
) : UserPersistencePort {

    override fun save(user: User): User {
        val entity = UserJpaEntity(
            id = user.id?.value,
            name = user.name,
            email = user.email,
            createdAt = user.createdAt
        )
        val saved = repo.save(entity)
        return saved.toDomain()
    }

    override fun findById(id: Long): User? = repo.findById(id).map { it.toDomain() }.orElse(null)

    override fun findAll(): List<User> = repo.findAll().map { it.toDomain() }

    override fun deleteById(id: Long) = repo.deleteById(id)

    override fun existsByEmail(email: String): Boolean = repo.existsByEmail(email)
}

private fun UserJpaEntity.toDomain(): User =
    User(id = id?.let { UserId(it) }, name = name, email = email, createdAt = createdAt)
