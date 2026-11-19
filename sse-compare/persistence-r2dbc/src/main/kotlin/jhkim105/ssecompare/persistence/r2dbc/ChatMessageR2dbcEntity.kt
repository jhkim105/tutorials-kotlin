package jhkim105.ssecompare.persistence.r2dbc

import jhkim105.ssecompare.common.MessageRole
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("chat_message")
data class ChatMessageR2dbcEntity(
    @Id
    val id: Long? = null,

    @Column("stream_id")
    val streamId: String,

    @Column("role")
    val role: MessageRole,

    @Column("content")
    val content: String,

    @Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
