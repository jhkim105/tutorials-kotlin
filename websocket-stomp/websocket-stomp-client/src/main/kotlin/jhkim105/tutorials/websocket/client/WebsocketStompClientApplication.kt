package jhkim105.tutorials.websocket.client

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WebsocketStompClientApplication

fun main(args: Array<String>) {
    runApplication<WebsocketStompClientApplication>(*args)
}
