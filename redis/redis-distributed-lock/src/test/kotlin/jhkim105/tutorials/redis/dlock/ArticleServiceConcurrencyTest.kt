package jhkim105.tutorials.redis.dlock

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import jhkim105.tutorials.redis.dlock.persistence.Article
import jhkim105.tutorials.redis.dlock.persistence.ArticleRepository
import jhkim105.tutorials.redis.dlock.service.ArticleService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ArticleServiceConcurrencyTest @Autowired constructor(
    private val articleRepository: ArticleRepository,
    private val articleService: ArticleService
) : StringSpec({

    "동시에 여러 사용자가 게시글 조회 시 viewCount가 정확히 증가해야 한다" {
        // given
        val article = Article(title = "테스트", content = "내용", viewCount = 0)
        val saved = articleRepository.save(article)
        val threadCount = 100
        val latch = CountDownLatch(threadCount)
        val executor = Executors.newFixedThreadPool(threadCount)

        // when
        repeat(threadCount) {
            executor.submit {
                try {
                    articleService.readArticle(saved.id!!)
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()

        // then
        val updated = articleRepository.getById(saved.id!!)
        updated.viewCount shouldBe threadCount
    }
})