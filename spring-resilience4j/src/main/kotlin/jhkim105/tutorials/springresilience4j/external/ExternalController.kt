package jhkim105.tutorials.springresilience4j.external

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/external")
class ExternalController {

    private val log = KotlinLogging.logger { }

    @GetMapping
    fun getWithDelay(delay: Long? = null): ExternalResponse {
        log.info { "called. delay: $delay" }
        val result = ExternalResponse(delay)
        delay?.let { Thread.sleep(delay) }
        return result
    }

    data class ExternalResponse(
        val delay: Long?
    )
}