package jhkim105.tutorials.kafka.persistence

import org.springframework.data.repository.CrudRepository

interface MessageJpaRepository : CrudRepository<MessageEntity, String> {
}