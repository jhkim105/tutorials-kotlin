package jhkim105.tutorials.kafka.persistence

import io.hypersistence.utils.hibernate.id.Tsid
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Duration
import java.time.Instant


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
    val durationMillis: Long,
) {
    companion object {
        fun of(key: String, publishAt: Instant): MessageEntity {
            val now = Instant.now()
            return MessageEntity(key = key, publishAt = publishAt, createdAt = now, durationMillis = Duration.between(publishAt, now).toMillis())
        }
    }
}