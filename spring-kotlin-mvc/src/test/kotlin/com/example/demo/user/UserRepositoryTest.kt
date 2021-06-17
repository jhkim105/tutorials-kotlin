package com.example.demo.user

import com.example.demo.JpaConfig
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlConfig

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaConfig::class)
class UserRepositoryTest @Autowired constructor (
    val entityManager: TestEntityManager,
    val userRepository: UserRepository) {

    @Test
    @Sql(scripts = ["/sql/user.sql"], config = SqlConfig(encoding = "UTF8"))
    fun findByUsername() {
        val user = userRepository.findByUsername("testuser01");
        assertThat(user).isNotNull;
    }

    @Test
    fun findAll() {
        val pageRequest = PageRequest.of(0, 10);
        val page = userRepository.findAll(pageRequest)
        println(page.content)
        assertThat(page.content.size).isEqualTo(10)
    }

    @Test
    fun save() {
        val user = User(username = "user01", password="pass1111", name ="User 01")
        userRepository.save(user)
        assertThat(user.id).isNotNull
    }

    @Test
    fun findAllByCompanyName() {
        val list = userRepository.findAllByCompanyName(companyName = "Company 01")
        assertThat(list).isNotEmpty
    }

}