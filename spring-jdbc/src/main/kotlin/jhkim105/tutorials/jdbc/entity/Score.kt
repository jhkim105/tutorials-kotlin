package jhkim105.tutorials.jdbc.entity

data class Score(
    val id: Long,
    val userId: String,
    val itemCode: String,
    val score: Double
)
