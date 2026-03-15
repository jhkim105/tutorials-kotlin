package jhkim105.tutorials.webclient

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ClientController(
    private val clientService: ClientService
) {

    @GetMapping("/client")
    suspend fun callApi(delay: Long = 0): String {
        return clientService.call(delay)
    }

    @GetMapping("/client_retry")
    suspend fun callAndRetry(delay: Long = 0): String {
        return clientService.callAndRetry(delay)
    }
}