package jhkim105.tutorials.validation

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<Map<String, String?>> {
        log.warn(ex) {"입력값 유효성 검증 실패. ex: $ex"}
        val errors = ex.bindingResult.fieldErrors.associate { it.field to it.defaultMessage }
        return ResponseEntity(errors, HttpStatus.BAD_REQUEST)
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(ex: ConstraintViolationException): ResponseEntity<Map<String, String?>> {
        log.warn(ex) {"입력값 유효성 검증 실패 (PathVariable, RequestParam). ex: $ex"}

        val errors = ex.constraintViolations.associate {
            val field = it.propertyPath.toString().split('.').last() // 예: getUserById.id → id
            field to it.message
        }
        return ResponseEntity(errors, HttpStatus.BAD_REQUEST)
    }
}
