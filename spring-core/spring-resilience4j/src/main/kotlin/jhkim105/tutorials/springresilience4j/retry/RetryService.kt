package jhkim105.tutorials.springresilience4j.retry

interface RetryService {
    fun processSomething(id: Int, maxRetry: Int = 3): String
}