package jhkim105.tutorials.jdbc

import jhkim105.tutorials.jdbc.entity.Score
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.test.context.jdbc.Sql


@JdbcTest
@Sql(scripts = ["/score/schema.sql", "/score/data.sql"])
class NamedParameterJdbcTemplateTests {

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    lateinit var namedJdbcTemplate: NamedParameterJdbcTemplate

    @Test
    fun fetchList() {
        val sql = """
            select * from score where user_id in (:ids)
        """.trimIndent()
        val ids = listOf("user1", "user2")
        val param = mapOf("ids" to ids)
        val list: List<Score> = namedJdbcTemplate.query(sql, param) { rs, rowNum ->
            Score(
               id = rs.getLong("id"),
               userId = rs.getString("user_id"),
               itemCode  = rs.getString("item_code"),
               score = rs.getDouble("score"),
            )

        }
        println(list)
    }


}