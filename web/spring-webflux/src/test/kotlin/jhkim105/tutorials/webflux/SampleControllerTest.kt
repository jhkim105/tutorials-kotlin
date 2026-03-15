package jhkim105.tutorials.webflux

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(SampleController::class)
class SampleControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun `sample should return Hello, world!`() {
        webTestClient.get()
            .uri("/samples")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .consumeWith { response ->
                assert(response.responseBody != null)
                val body = String(response.responseBody!!)
                assert(body == "Hello, world!")
            }
    }
}
