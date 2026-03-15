package jhkim105.tutorials.springresilience4j.api

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RequestMapping("/api")
@RestController
class ApiController(
    private val externalApiService: ExternalApiService
) {

    @GetMapping("/circuit-breaker")
    @CircuitBreaker(name = "myCircuit")
    fun circuitBreaker(delay: Long? = null): ApiResponse {
        val result: String? =
            delay?.let { externalApiService.callApiWithDelay(delay) } ?: run { externalApiService.callApi() }
        return ApiResponse(result)
    }

    data class ApiResponse(val message: String?)
}