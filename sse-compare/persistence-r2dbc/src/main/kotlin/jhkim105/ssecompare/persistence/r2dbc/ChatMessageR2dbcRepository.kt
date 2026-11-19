package jhkim105.ssecompare.persistence.r2dbc

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux

interface ChatMessageR2dbcRepository : ReactiveCrudRepository<ChatMessageR2dbcEntity, Long> {
    fun findByStreamIdOrderByIdAsc(streamId: String): Flux<ChatMessageR2dbcEntity>
}
