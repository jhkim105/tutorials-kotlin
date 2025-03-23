package jhkim105.tutorials.redis.dlock.persistence

import io.hypersistence.utils.hibernate.id.Tsid
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime


@Entity
@Table(name = "t_messages")
class MessageEntity(
    @Id @Tsid
    var id: String? = null,
    @Column(name = "message_key", length = 64, unique = true, nullable = false)
    val key: String,
    @Column(nullable = false)
    val publishAt: Instant,
    @Column(nullable = false)
    val createdAt: Instant,
    @Column(nullable = false)
    val duration: Duration,
) {
    companion object {
        fun of(key: String, publishAt: Instant): MessageEntity {
            val now = Instant.now()
            return MessageEntity(key = key, publishAt = publishAt, createdAt = now, duration = Duration.between(publishAt, now))
        }
    }
}