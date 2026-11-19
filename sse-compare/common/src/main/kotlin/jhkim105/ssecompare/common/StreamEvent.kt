package jhkim105.ssecompare.common

object StreamEventNames {
    const val MESSAGE = "message"
    const val DONE = "done"
    const val ERROR = "error"
    const val HEARTBEAT_COMMENT = "ping"
}

object StreamErrorCodes {
    const val INVALID_REQUEST = "INVALID_REQUEST"
    const val SIMULATED_ERROR = "SIMULATED_ERROR"
    const val DB_ERROR = "DB_ERROR"
    const val DB_TIMEOUT = "DB_TIMEOUT"
    const val INTERNAL_ERROR = "INTERNAL_ERROR"
}

data class MessageEventData(
    val seq: Int,
    val content: String,
    val serverTs: Long,
)

data class DoneEventData(
    val total: Int,
    val serverTs: Long,
)

data class ErrorEventData(
    val code: String,
    val message: String,
)
