package jhkim105.tutorials.jdbc.rowtocol

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class ScoreRepository(
    private val jdbcTemplate: JdbcTemplate,
) {

    fun findAll(): List<Map<String, Any>> {
        return jdbcTemplate.query("select id, user_id, item_code, score from score") { rs, _ ->
            mapOf(
                "id" to rs.getLong("id"),
                "user_id" to rs.getString("user_id"),
                "item_code" to rs.getString("item_code"),
                "score" to rs.getInt("score")
            )
        }
    }

    fun getUserScores(): List<Map<String, Any>> {
        val rawData = findAll()

        val userScoreMap = mutableMapOf<String, MutableMap<String, Any>>()

        for (row in rawData) {
            val userId = row["user_id"] as String
            val itemCode = row["item_code"] as String
            val score = row["score"] as Int

            val userMap = userScoreMap.getOrPut(userId) {
                mutableMapOf("user_id" to userId)
            }

            userMap[itemCode] = score
        }

        return userScoreMap.values.toList()
    }
}

