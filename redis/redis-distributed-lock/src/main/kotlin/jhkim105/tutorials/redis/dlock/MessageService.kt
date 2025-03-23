package jhkim105.tutorials.redis.dlock

import jhkim105.tutorials.redis.dlock.aop.DistributedExecuteOnce
import jhkim105.tutorials.redis.dlock.persistence.MessageEntity
import jhkim105.tutorials.redis.dlock.persistence.MessageJpaRepository
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class MessageService(
    private val messageJpaRepository: MessageJpaRepository
) {


    @DistributedExecuteOnce(key= "#key", leaseTime = 100)
    fun saveMessage(key: String, publishedAt: Instant) {
        val messageEntity = MessageEntity.of(key, publishedAt)
        messageJpaRepository.save(messageEntity)
    }

}