package domain.service

import domain.model.Article
import domain.model.Tag

class ArticleDomainService {

    fun createArticle(title: String, content: String, tagNames: List<String>): Article {
        val tags = tagNames.map { Tag(name = it) }
        return Article(title = title, content = content, tags = tags)
    }
}