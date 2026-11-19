package jhkim105.ssecompare.persistence.r2dbc

import jhkim105.ssecompare.common.MessageRole
import jhkim105.ssecompare.common.ReactiveChatMessageStore
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Configuration
@Profile("db-r2dbc")
@EnableR2dbcRepositories(basePackages = ["jhkim105.ssecompare.persistence.r2dbc"])
class R2dbcPersistenceConfig

@Service
@Profile("db-r2dbc")
class R2dbcChatMessageStore(
    private val repository: ChatMessageR2dbcRepository,
) : ReactiveChatMessageStore {
    override suspend fun save(streamId: String, role: MessageRole, content: String) {
        saveMono(streamId, role, content).awaitSingle()
    }

    fun saveMono(streamId: String, role: MessageRole, content: String): Mono<ChatMessageR2dbcEntity> {
        return repository.save(
            ChatMessageR2dbcEntity(
                streamId = streamId,
                role = role,
                content = content,
            ),
        )
    }
}
