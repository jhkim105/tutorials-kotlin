package jhkim105.ssecompare.loadgen.scenario

import io.github.oshai.kotlinlogging.KotlinLogging
import jhkim105.ssecompare.common.StreamRequest
import jhkim105.ssecompare.loadgen.LimitDetector
import jhkim105.ssecompare.loadgen.MetricsRecorder
import jhkim105.ssecompare.loadgen.ResultWriter
import jhkim105.ssecompare.loadgen.ScenarioConfig
import jhkim105.ssecompare.loadgen.SseClient
import jhkim105.ssecompare.loadgen.StageResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

private val logger = KotlinLogging.logger {}

object S6SlowClient {
    suspend fun run(
        client: SseClient,
        targetUrl: String,
        config: ScenarioConfig,
        resultWriter: ResultWriter,
    ): StageResult {
        val connections = config.connections.coerceAtLeast(1)
        logger.info { "[S6] Starting slow client: connections=$connections, readDelayMs=${config.readDelayMs}" }

        val recorder = MetricsRecorder()
        val req = StreamRequest(
            prompt = "bench-S6-slow",
            events = config.events,
            intervalMs = config.intervalMs,
            persist = config.persist,
        )

        val startMs = System.currentTimeMillis()
        coroutineScope {
            val jobs = List(connections) {
                launch(Dispatchers.Default) {
                    val outcome = client.streamPost(
                        url = targetUrl,
                        request = req,
                        readDelayMs = config.readDelayMs,
                    )
                    recorder.record(outcome)
                }
            }
            jobs.joinAll()
        }
        val durationMs = System.currentTimeMillis() - startMs
        val clientSnap = recorder.snapshot(durationMs)
        val serverSnap = resultWriter.snapshotServerMetrics()
        val limit = LimitDetector.evaluate(clientSnap, config.intervalMs + config.readDelayMs)

        return StageResult(
            stageName = "slow_c=$connections",
            targetConnections = connections,
            client = clientSnap,
            server = serverSnap,
            limitReached = limit.reached,
            limitReason = limit.reason,
        )
    }
}
