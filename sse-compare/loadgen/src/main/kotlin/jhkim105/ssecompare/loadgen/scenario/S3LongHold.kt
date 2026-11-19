package jhkim105.ssecompare.loadgen.scenario

import io.github.oshai.kotlinlogging.KotlinLogging
import jhkim105.ssecompare.common.StreamRequest
import jhkim105.ssecompare.loadgen.LimitDetector
import jhkim105.ssecompare.loadgen.MetricsRecorder
import jhkim105.ssecompare.loadgen.ResultWriter
import jhkim105.ssecompare.loadgen.ScenarioConfig
import jhkim105.ssecompare.loadgen.ServerMetricsSnapshot
import jhkim105.ssecompare.loadgen.SseClient
import jhkim105.ssecompare.loadgen.StageResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

private val logger = KotlinLogging.logger {}

object S3LongHold {
    suspend fun run(
        client: SseClient,
        targetUrl: String,
        config: ScenarioConfig,
        resultWriter: ResultWriter,
    ): Pair<StageResult, List<ServerMetricsSnapshot>> {
        val connections = config.connections.coerceAtLeast(1)
        logger.info { "[S3] Starting long hold: connections=$connections, events=${config.events}, intervalMs=${config.intervalMs}" }

        val recorder = MetricsRecorder()
        val trendSamples = mutableListOf<ServerMetricsSnapshot>()
        val req = StreamRequest(
            prompt = "bench-S3-long",
            events = config.events,
            intervalMs = config.intervalMs,
            persist = config.persist,
        )

        val startMs = System.currentTimeMillis()
        coroutineScope {
            val sampler = launch(Dispatchers.IO) {
                while (isActive) {
                    trendSamples.add(resultWriter.snapshotServerMetrics())
                    delay(5_000L)
                }
            }
            val jobs = List(connections) {
                launch(Dispatchers.Default) {
                    val outcome = client.streamPost(targetUrl, req)
                    recorder.record(outcome)
                }
            }
            jobs.joinAll()
            sampler.cancel()
        }
        val durationMs = System.currentTimeMillis() - startMs
        val clientSnap = recorder.snapshot(durationMs)
        val serverSnap = resultWriter.snapshotServerMetrics()
        val limit = LimitDetector.evaluate(clientSnap, config.intervalMs)

        return StageResult(
            stageName = "hold_c=$connections",
            targetConnections = connections,
            client = clientSnap,
            server = serverSnap,
            limitReached = limit.reached,
            limitReason = limit.reason,
        ) to trendSamples
    }
}
