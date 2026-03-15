package jhkim105.tutorials.filter

import jakarta.servlet.FilterChain
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.util.StopWatch
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper
import java.io.IOException
import java.nio.charset.StandardCharsets

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
class LoggingFilter(
    private val serverProperties: ServerProperties
) : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(javaClass)
    private val ignoredPaths = listOf("/api-docs", "/swagger", "/version.txt")

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val contextPath = serverProperties.servlet.contextPath ?: ""
        return ignoredPaths.any { path -> request.requestURI.startsWith(contextPath + path) }
    }

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val cachingRequest = ContentCachingRequestWrapper(request)
        val cachingResponse = ContentCachingResponseWrapper(response)

        val stopWatch = StopWatch().apply { start() }

        try {
            filterChain.doFilter(cachingRequest, cachingResponse)
        } finally {
            stopWatch.stop()
            log.info(buildLog(cachingRequest, cachingResponse, stopWatch.totalTimeMillis))
            cachingResponse.copyBodyToResponse()
        }
    }

    private fun buildLog(
        request: ContentCachingRequestWrapper,
        response: ContentCachingResponseWrapper,
        elapsedTime: Long
    ): String {
        return """
            |${request.method} ${request.requestURI} (${HttpStatus.valueOf(response.status)}) (${elapsedTime}ms)
            |>> request parameter: ${formatParameters(request)}
            |>> request body: ${readRequestBody(request)}
            |>> response body: ${readResponseBody(response)}
        """.trimMargin()
    }

    private fun formatParameters(request: ContentCachingRequestWrapper): String {
        return request.parameterMap.entries.joinToString("&") { (key, values) ->
            "$key=${values.joinToString(",")}"
        }
    }

    private fun readRequestBody(request: ContentCachingRequestWrapper): String {
        return String(request.contentAsByteArray, StandardCharsets.UTF_8)
    }

    private fun readResponseBody(response: ContentCachingResponseWrapper): String {
        val contentType = response.contentType ?: return "[no content type]"
        return if (isBinaryContentType(contentType)) {
            "[binary content - $contentType, omitted]"
        } else {
            String(response.contentAsByteArray, StandardCharsets.UTF_8)
        }
    }

    private fun isBinaryContentType(contentType: String): Boolean {
        return listOf(
            "application/octet-stream",
            "application/pdf",
            "application/zip",
            "image/", "audio/", "video/"
        ).any { contentType.startsWith(it) }
    }
}