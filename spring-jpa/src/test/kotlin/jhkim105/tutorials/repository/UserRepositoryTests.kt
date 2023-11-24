package jhkim105.tutorials.repository

import jhkim105.tutorials.JpaConfig
import jhkim105.tutorials.domain.User
import jhkim105.tutorials.domain.UserType
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
class UserRepositoryTests @Autowired constructor (
    val userRepository: UserRepository) {

    val log:Logger = LoggerFactory.getLogger(javaClass)
    @Test
    @Sql(scripts = ["/sql/user.sql"], config = SqlConfig(encoding = "UTF8"))
    fun findByUsername() {
        val user = userRepository.findByUsername("testuser01");
        log.info("$user")
        Assertions.assertThat(user).isNotNull;
        Assertions.assertThat(user!!.stringList).hasSize(2);
    }

    @Test
    @Sql(scripts = ["/sql/user.sql"], config = SqlConfig(encoding = "UTF8"))
    fun getReferenceById() {
        val user = userRepository.getReferenceById("test-id01");
        Assertions.assertThat(user).isNotNull;
    }

    @Test
    fun findAll() {
        val pageRequest = PageRequest.of(0, 10);
        val page = userRepository.findAll(pageRequest)
        println(page.content)
        Assertions.assertThat(page.content.size).isEqualTo(10)
    }

    @Test
    fun save() {
        val user = User(username = "user01", password="pass1111", name ="User 01")
        user.userType = UserType.ADMIN
        userRepository.save(user)
        Assertions.assertThat(user.id).isNotNull
    }

    @Test
    fun findAllByCompanyName() {
        val list = userRepository.findAllByCompanyName(companyName = "Company 01")
        Assertions.assertThat(list).isNotEmpty
    }

}