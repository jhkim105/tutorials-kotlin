package domain.model

data class Article(
    val id: String? = null,
    val title: String,
    val content: String,
    val tags: List<Tag>
)
