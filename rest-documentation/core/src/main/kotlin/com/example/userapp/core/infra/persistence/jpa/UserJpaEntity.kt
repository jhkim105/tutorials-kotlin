package com.example.userapp.core.infra.persistence.jpa

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "users", indexes = [Index(name = "uk_users_email", columnList = "email", unique = true)])
class UserJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var name: String = "",

    @Column(nullable = false, unique = true)
    var email: String = "",

    @Column(nullable = false)
    var createdAt: Instant = Instant.now()
)
