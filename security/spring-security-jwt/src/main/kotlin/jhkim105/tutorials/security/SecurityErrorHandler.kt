package jhkim105.tutorials.security

import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.http.server.ServletServerHttpResponse
import org.springframework.stereotype.Component

@Component
class SecurityErrorHandler(
    private val jacksonConverter: MappingJackson2HttpMessageConverter,
) {

    fun handleUnauthorized(response: HttpServletResponse) {
        writeError(response, HttpStatus.UNAUTHORIZED, "Unauthorized")
    }

    fun handleAccessDenied(response: HttpServletResponse) {
        writeError(response, HttpStatus.FORBIDDEN, "Forbidden")
    }

    private fun writeError(response: HttpServletResponse, status: HttpStatus, message: String) {
        val errorAttributes = mapOf(
            "status" to status.value(),
            "message" to message,
        )
        response.status = status.value()
        jacksonConverter.write(errorAttributes, MediaType.APPLICATION_JSON, ServletServerHttpResponse(response))
    }
}
