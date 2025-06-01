package jhkim105.tutorials.redis.dlock.persistence

import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Entity
@Table(name = "t_article")
class Article(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val title: String,
    val content: String,
    var viewCount: Long = 0
)


interface ArticleJpaRepository : JpaRepository<Article, Long> {}

@Repository
class ArticleRepository(
    private val articleJpaRepository: ArticleJpaRepository
){

    fun getById(id: Long) = articleJpaRepository.findByIdOrNull(id) ?: throw EntityNotFoundException("Article with id $id not found")

    fun save(article: Article): Article {
        return articleJpaRepository.save(article)
    }

    @Transactional
    fun readArticle(id : Long) {
        val article = getById(id)
        article.viewCount++
        articleJpaRepository.save(article)
    }
}