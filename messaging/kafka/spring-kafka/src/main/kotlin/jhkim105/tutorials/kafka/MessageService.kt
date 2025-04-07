package jhkim105.tutorials.kafka

import jhkim105.tutorials.kafka.persistence.MessageEntity
import jhkim105.tutorials.kafka.persistence.MessageJpaRepository
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class MessageService(
    private val messageJpaRepository: MessageJpaRepository
) {

    fun saveMessage(key: String, publishedAt: Instant) {
        val messageEntity = MessageEntity.of(key, publishedAt)
        messageJpaRepository.save(messageEntity)
    }

}