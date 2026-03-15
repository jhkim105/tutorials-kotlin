package jhkim105.tutorials.springresilience4j.retry

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController()
@RequestMapping("/retries")
class RetryController(
    private val retryService: RetryService
) {

    @PostMapping("/{id}")
    fun process(@PathVariable id: Int, maxRetry:Int = 3): PaymentResponse{
        val message = retryService.processSomething(1, maxRetry)
        return PaymentResponse(message)
    }

    data class PaymentResponse(
        val message: String
    )
}