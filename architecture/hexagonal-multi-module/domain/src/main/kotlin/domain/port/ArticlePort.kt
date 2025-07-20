package domain.port

import domain.model.Article

interface ArticlePort {
    fun save(article: Article): Article
    fun findById(id: String): Article?
}