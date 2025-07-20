package jhkim105.tutorials.websocket.server.auth

import java.security.Principal

class StompPrincipal(private val name: String) : Principal {
    override fun getName(): String = name
}
