package com.example.userapp.core.domain

import java.time.Instant
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

@JvmInline
value class UserId(val value: Long)


data class User(
    val id: UserId? = null,
    @field:NotBlank val name: String,
    @field:Email val email: String,
    val createdAt: Instant = Instant.now()
)
