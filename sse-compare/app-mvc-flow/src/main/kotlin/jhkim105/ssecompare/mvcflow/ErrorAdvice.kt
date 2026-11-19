package jhkim105.ssecompare.mvcflow

import jakarta.validation.ConstraintViolationException
import jhkim105.ssecompare.common.ErrorResponse
import jhkim105.ssecompare.common.StreamErrorCodes
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.HandlerMethodValidationException

@RestControllerAdvice
class ErrorAdvice {
    @ExceptionHandler(
        MethodArgumentNotValidException::class,
        BindException::class,
        ConstraintViolationException::class,
        HandlerMethodValidationException::class,
        HttpMessageNotReadableException::class,
    )
    fun handleValidation(ex: Exception): ResponseEntity<ErrorResponse> {
        val message = when (ex) {
            is MethodArgumentNotValidException ->
                ex.bindingResult.allErrors.joinToString("; ") { it.defaultMessage ?: "invalid" }
            is BindException ->
                ex.bindingResult.allErrors.joinToString("; ") { it.defaultMessage ?: "invalid" }
            else -> ex.message ?: "Invalid request"
        }
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(code = StreamErrorCodes.INVALID_REQUEST, message = message))
    }
}
