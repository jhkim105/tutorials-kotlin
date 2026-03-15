package jhkim105.tutorials.user.repository

import jhkim105.tutorials.JpaConfig
import jhkim105.tutorials.user.entity.User
import jhkim105.tutorials.user.entity.UserType
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlConfig


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaConfig::class)
class UserJpaRepositoryTests @Autowired constructor (
    val userJpaRepository: UserJpaRepository
) {

    val log:Logger = LoggerFactory.getLogger(javaClass)
    @Test
    @Sql(scripts = ["/sql/user.sql"], config = SqlConfig(encoding = "UTF8"))
    fun findByUsername() {
        val user = userJpaRepository.findByUsername("testuser01");
        log.info("$user")
        Assertions.assertThat(user).isNotNull;
        Assertions.assertThat(user!!.stringList).hasSize(2);
    }

    @Test
    @Sql(scripts = ["/sql/user.sql"], config = SqlConfig(encoding = "UTF8"))
    fun getReferenceById() {
        val user = userJpaRepository.getReferenceById("test-id01");
        Assertions.assertThat(user).isNotNull;
    }

    @Test
    fun findAll() {
        val pageRequest = PageRequest.of(0, 10);
        val page = userJpaRepository.findAll(pageRequest)
        println(page.content)
        Assertions.assertThat(page.content.size).isEqualTo(10)
    }

    @Test
    fun save() {
        val user = User(username = "testuser01", password="pass1111", name ="User 01", userType = UserType.ADMIN)
        userJpaRepository.save(user)
        Assertions.assertThat(user.id).isNotNull
    }


    @Test
    fun create() {
        (1..10).forEach {
            val formattedIndex = String.format("%02d", it)
            userJpaRepository.save(User(username = "tuser_$formattedIndex", password="pass1111", name ="User $formattedIndex", userType = UserType.ADMIN))
        }
        val list = userJpaRepository.findAll()
        log.debug("$list")
    }

}