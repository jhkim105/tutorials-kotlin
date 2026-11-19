package jhkim105.ssecompare.common

enum class MessageRole {
    USER,
    ASSISTANT,
}

/**
 * Blocking persistence port (used by MVC apps and WebFlux db-jpa isolated mode).
 */
interface BlockingChatMessageStore {
    fun save(streamId: String, role: MessageRole, content: String)
}

/**
 * Non-blocking persistence port (used by WebFlux db-r2dbc mode).
 */
interface ReactiveChatMessageStore {
    suspend fun save(streamId: String, role: MessageRole, content: String)
}

class SimulatedStreamException(
    val code: String = StreamErrorCodes.SIMULATED_ERROR,
    override val message: String,
) : RuntimeException(message)
