package com.example.userapp.core.infra.persistence.jpa

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ContextConfiguration
import java.time.Instant

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = [TestJpaConfig::class])
class UserJpaRepositoryTest @Autowired constructor(
    private val repo: UserJpaRepository
) {

    @Test
    fun `유저 저장 및 조회`() {
        val user = UserJpaEntity(name = "Bob", email = "bob@example.com", createdAt = Instant.now())
        repo.save(user)
        val found = repo.findAll().first()
        assert(found.email == "bob@example.com")
    }
}