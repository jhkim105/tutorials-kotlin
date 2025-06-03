package jhkim105.tutorials.websocket.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WebsocketStompServerApplication

fun main(args: Array<String>) {
	runApplication<WebsocketStompServerApplication>(*args)
}
