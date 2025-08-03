package jhkim105.tutorials.springresilience4j.retry

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController()
@RequestMapping("/payments")
class PaymentController(
    private val paymentService: PaymentService
) {

    @PostMapping("/process/{id}")
    fun process(@PathVariable id: Int, maxRetry:Int = 3): PaymentResponse{
        val message = paymentService.processPayment(1, maxRetry)
        return PaymentResponse(message)
    }

    data class PaymentResponse(
        val message: String
    )
}