import java.util.concurrent.atomic.AtomicInteger

class SampleService {
    private val counter = AtomicInteger(0)

    fun unreliableMethod(): String {
        val attempt = counter.incrementAndGet()
        if (attempt < 3) {
            throw RuntimeException("Failure on attempt $attempt")
        }
        return "Success on attempt $attempt"
    }
}