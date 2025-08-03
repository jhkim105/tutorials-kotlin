package jhkim105.tutorials.springresilience4j.retry

interface PaymentService {
    fun processPayment(id: Int, maxRetry: Int = 3): String
}