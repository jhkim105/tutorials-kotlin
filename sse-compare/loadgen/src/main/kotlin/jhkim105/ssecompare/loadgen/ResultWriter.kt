package jhkim105.ssecompare.loadgen

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration

private val logger = KotlinLogging.logger {}

class ResultWriter(
    private val prometheusUrl: String? = null,
    private val prometheusJob: String? = null,
) {
    private val objectMapper = jacksonObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
    private val httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build()

    fun snapshotServerMetrics(): ServerMetricsSnapshot {
        if (prometheusUrl.isNullOrBlank()) return ServerMetricsSnapshot()
        val jobFilter = if (prometheusJob.isNullOrBlank()) "" else """job="$prometheusJob""""
        fun q(metric: String, extra: String = ""): Double? {
            val selector = listOf(jobFilter, extra).filter { it.isNotBlank() }.joinToString(",")
            val expr = if (selector.isBlank()) metric else "$metric{$selector}"
            return queryPrometheus("sum($expr)")
        }
        return ServerMetricsSnapshot(
            activeConnections = q("sse_active_connections"),
            liveThreads = q("jvm_threads_live_threads"),
            tomcatBusyThreads = q("tomcat_threads_busy_threads"),
            cpuPct = q("process_cpu_usage")?.times(100.0),
            heapMb = q("jvm_memory_used_bytes", """area="heap"""")?.div(1024.0 * 1024.0),
            gcPauseSecPerSec = if (jobFilter.isBlank()) {
                queryPrometheus("sum(rate(jvm_gc_pause_seconds_sum[30s]))")
            } else {
                queryPrometheus("""sum(rate(jvm_gc_pause_seconds_sum{$jobFilter}[30s]))""")
            },
            hikariActive = q("hikaricp_connections_active"),
            hikariPending = q("hikaricp_connections_pending"),
            r2dbcAcquired = q("r2dbc_pool_acquired_size"),
        )
    }

    private fun queryPrometheus(expr: String): Double? {
        return try {
            val encoded = URLEncoder.encode(expr, StandardCharsets.UTF_8)
            val req = HttpRequest.newBuilder()
                .uri(URI.create("$prometheusUrl/api/v1/query?query=$encoded"))
                .timeout(Duration.ofSeconds(3))
                .GET()
                .build()
            val resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString())
            if (resp.statusCode() != 200) return null
            val root = objectMapper.readTree(resp.body())
            val results = root.path("data").path("result")
            if (!results.isArray || results.isEmpty) return null
            results[0].path("value").get(1)?.asText()?.toDoubleOrNull()
        } catch (e: Exception) {
            logger.debug { "Prometheus query failed ($expr): ${e.message}" }
            null
        }
    }

    fun writeResult(outDir: File, result: BenchmarkRunResult): File {
        outDir.mkdirs()
        val file = File(outDir, "${result.combination}-${result.scenario}-run${result.run}.json")
        objectMapper.writeValue(file, result)
        logger.info { "Saved benchmark result to ${file.absolutePath}" }
        return file
    }
}
