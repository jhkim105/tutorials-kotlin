package com.example.userapp.core.infra.persistence.jpa

import org.springframework.data.jpa.repository.JpaRepository

interface UserJpaRepository : JpaRepository<UserJpaEntity, Long> {
    fun existsByEmail(email: String): Boolean
}
