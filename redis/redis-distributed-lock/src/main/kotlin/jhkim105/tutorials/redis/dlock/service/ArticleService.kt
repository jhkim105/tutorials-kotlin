package jhkim105.tutorials.redis.dlock.service

import jhkim105.tutorials.redis.dlock.aop.DistributedLock
import jhkim105.tutorials.redis.dlock.persistence.ArticleRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ArticleService(
    private val articleRepository: ArticleRepository
) {

    @DistributedLock(key = "#id")
    @Transactional
    fun readArticle(id : Long) {
        articleRepository.readArticle(id)
    }
}