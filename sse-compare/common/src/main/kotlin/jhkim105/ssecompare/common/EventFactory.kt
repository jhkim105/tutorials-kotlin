package jhkim105.ssecompare.common

object EventFactory {
    private val paddingCache = mutableMapOf<Int, String>()

    fun createMessage(seq: Int, payloadBytes: Int = 64): MessageEventData {
        val prefix = "chunk-$seq:"
        val content = if (prefix.length >= payloadBytes) {
            prefix.substring(0, payloadBytes)
        } else {
            val padLen = payloadBytes - prefix.length
            val pad = paddingCache.computeIfAbsent(padLen) { "x".repeat(it) }
            prefix + pad
        }
        return MessageEventData(
            seq = seq,
            content = content,
            serverTs = System.currentTimeMillis(),
        )
    }

    fun createDone(total: Int): DoneEventData = DoneEventData(
        total = total,
        serverTs = System.currentTimeMillis(),
    )

    fun createError(code: String, message: String): ErrorEventData = ErrorEventData(
        code = code,
        message = message,
    )
}
