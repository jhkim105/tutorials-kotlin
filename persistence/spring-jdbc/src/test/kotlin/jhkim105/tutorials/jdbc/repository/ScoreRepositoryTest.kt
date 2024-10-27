package jhkim105.tutorials.jdbc.repository

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.jdbc.Sql

@JdbcTest
@Import(ScoreRepository::class)
@Sql(scripts = ["/score/schema.sql", "/score/data.sql"])
class ScoreRepositoryTest {

    @Autowired
    lateinit var repository: ScoreRepository

    @Test
    fun findAll() {
        repository.findAll()
    }

    @Test
    fun getUserScores() {
        val result = repository.getUserScores()
        println(result)
    }


}