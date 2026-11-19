package jhkim105.ssecompare.persistence.jpa

import org.springframework.data.jpa.repository.JpaRepository

interface ChatMessageJpaRepository : JpaRepository<ChatMessageJpaEntity, Long> {
    fun findByStreamIdOrderByIdAsc(streamId: String): List<ChatMessageJpaEntity>
}
