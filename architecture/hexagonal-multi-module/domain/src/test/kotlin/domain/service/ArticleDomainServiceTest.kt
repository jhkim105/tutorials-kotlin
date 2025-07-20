package domain.service
import domain.model.Article
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ArticleDomainServiceTest {

    private val articleDomainService = ArticleDomainService()

    @Test
    fun `createArticle should create Article with given title, content, and tags`() {
        // given
        val title = "Test Article"
        val content = "This is test content"
        val tagNames = listOf("Kotlin", "Hexagonal", "Spring")

        // when
        val article: Article = articleDomainService.createArticle(title, content, tagNames)

        // then
        assertNotNull(article)
        assertNull(article.id) // 생성 시점에는 id가 없다고 가정
        assertEquals(title, article.title)
        assertEquals(content, article.content)
        assertEquals(tagNames.size, article.tags.size)
        assertTrue(article.tags.all { it.id == null }) // 생성 시점 태그 id도 없음
        assertEquals(tagNames, article.tags.map { it.name })
    }
}