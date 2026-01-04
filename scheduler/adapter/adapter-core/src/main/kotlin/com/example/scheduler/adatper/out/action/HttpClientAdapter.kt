package com.example.scheduler.adatper.out.action

import com.example.scheduler.application.port.HttpClientPort
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.stereotype.Component

@Component
class HttpClientAdapter(
    restTemplateBuilder: RestTemplateBuilder
) : HttpClientPort {
    private val restTemplate = restTemplateBuilder.build()

    override fun get(url: String): Int {
        return restTemplate.getForEntity(url, String::class.java).statusCode.value()
    }
}
