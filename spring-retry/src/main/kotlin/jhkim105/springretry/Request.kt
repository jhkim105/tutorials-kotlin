package jhkim105.springretry

data class Request(val id: String)
data class Response(val message: String)

class FlakyException(message: String): RuntimeException(message)
