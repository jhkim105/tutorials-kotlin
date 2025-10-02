package jhkim105.flexmark.html2md

import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException

@Component
class HtmlDownloader(
    private val restClient: RestClient
) {
    /** URL에서 HTML을 문자열로 반환 */
    fun fetch(url: String): String =
        try {
            restClient.get()
                .uri(url)
                .retrieve()
                .body(String::class.java)
                ?: error("Empty body from $url")
        } catch (e: RestClientResponseException) {
            throw IllegalStateException(
                "HTTP ${e.statusCode.value()} from $url: ${e.responseBodyAsString.take(200)}", e
            )
        }
}
