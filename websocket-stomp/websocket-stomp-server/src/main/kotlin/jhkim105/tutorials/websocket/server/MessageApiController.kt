package jhkim105.tutorials.websocket.server

import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class MessageApiController(
    private val messagingTemplate: SimpMessagingTemplate
) {
    data class MessageRequest(val message: String)

    @PostMapping("/send")
    fun sendMessage(@RequestBody request: MessageRequest): String {
        messagingTemplate.convertAndSend("/topic/message", request.message)
        return "OK"
    }
}
