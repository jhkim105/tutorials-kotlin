package jhkim105.tutorials.webflux.file

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.util.LinkedMultiValueMap

@WebFluxTest(FileUploadController::class)
class FileUploadControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun `should upload file successfully`() {
        val fileResource = ClassPathResource("file/sample.txt")
        webTestClient.post()
            .uri("/files")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .bodyValue(
                LinkedMultiValueMap<String, Any>().apply {
                    add("file", fileResource)
                })
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .consumeWith { response ->
                assert(response.responseBody != null)
                val body = String(response.responseBody!!)
                assert(body.contains("OK"))
            }
    }
}
