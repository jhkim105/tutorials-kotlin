package jhkim105.ssecompare.webfluxflux

import jakarta.validation.ConstraintViolationException
import jhkim105.ssecompare.common.ErrorResponse
import jhkim105.ssecompare.common.StreamErrorCodes
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.ServerWebInputException

@RestControllerAdvice
class ErrorAdvice {
    @ExceptionHandler(
        WebExchangeBindException::class,
        ServerWebInputException::class,
        ConstraintViolationException::class,
    )
    fun handleValidation(ex: Exception): ResponseEntity<ErrorResponse> {
        val message = when (ex) {
            is WebExchangeBindException ->
                ex.bindingResult.allErrors.joinToString("; ") { it.defaultMessage ?: "invalid" }
            else -> ex.message ?: "Invalid request"
        }
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(code = StreamErrorCodes.INVALID_REQUEST, message = message))
    }
}
