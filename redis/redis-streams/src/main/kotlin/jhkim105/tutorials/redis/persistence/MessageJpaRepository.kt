package jhkim105.tutorials.redis.dlock.persistence

import org.springframework.data.repository.CrudRepository

interface MessageJpaRepository : CrudRepository<MessageEntity, String> {
}