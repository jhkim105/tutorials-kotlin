package jhkim105.authzsecurity.security

import com.fasterxml.jackson.databind.ObjectMapper
import jhkim105.authzsecurity.common.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import jakarta.servlet.http.HttpServletResponse

@Component
class ErrorResponseWriter(
    private val objectMapper: ObjectMapper
) {

    fun write(response: HttpServletResponse, status: HttpStatus, message: String?) {
        response.status = status.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.writer.write(objectMapper.writeValueAsString(ErrorResponse(message)))
    }
}
