package jhkim105.ssecompare.persistence.jpa

import jhkim105.ssecompare.common.BlockingChatMessageStore
import jhkim105.ssecompare.common.MessageRole
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.stereotype.Service

@Configuration
@Profile("db-jpa")
@EntityScan(basePackages = ["jhkim105.ssecompare.persistence.jpa"])
@EnableJpaRepositories(basePackages = ["jhkim105.ssecompare.persistence.jpa"])
class JpaPersistenceConfig

@Service
@Profile("db-jpa")
class JpaChatMessageStore(
    private val repository: ChatMessageJpaRepository,
) : BlockingChatMessageStore {
    override fun save(streamId: String, role: MessageRole, content: String) {
        repository.save(
            ChatMessageJpaEntity(
                streamId = streamId,
                role = role,
                content = content,
            ),
        )
    }
}
