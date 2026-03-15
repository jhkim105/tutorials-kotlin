package jhkim105.tutorials.websocket

import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class MessageApiController(
    private val messageHandler: MessageWebSocketHandler
) {

    @PostMapping("/send")
    fun sendMessage(@RequestBody request: MessageRequest): String {
        messageHandler.broadcast(request.message)
        return "OK"
    }

    data class MessageRequest(val message: String)
}
