package jhkim105.tutorials.springresilience4j.api

import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class ExternalApiService(
    private val restTemplate: RestTemplate
) {

    fun callApi(): String? {
        return restTemplate.getForObject("/external", String::class.java)
    }

    fun callApiWithDelay(delay: Long): String? {
        val result = restTemplate.getForObject("/external?delay={delay}", String::class.java, delay)
        return result
    }
}