package jhkim105.ssecompare.persistence.jpa

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import jhkim105.ssecompare.common.MessageRole
import java.time.LocalDateTime

@Entity
@Table(
    name = "chat_message",
    indexes = [Index(name = "idx_chat_message_stream_id", columnList = "stream_id")],
)
class ChatMessageJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "stream_id", nullable = false, length = 36)
    val streamId: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 10)
    val role: MessageRole,

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    val content: String,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
