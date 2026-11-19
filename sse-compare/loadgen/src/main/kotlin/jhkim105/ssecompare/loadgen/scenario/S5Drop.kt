package jhkim105.ssecompare.loadgen.scenario

import io.github.oshai.kotlinlogging.KotlinLogging
import jhkim105.ssecompare.common.StreamRequest
import jhkim105.ssecompare.loadgen.LimitDetector
import jhkim105.ssecompare.loadgen.MetricsRecorder
import jhkim105.ssecompare.loadgen.RecoveryCheck
import jhkim105.ssecompare.loadgen.ResultWriter
import jhkim105.ssecompare.loadgen.ScenarioConfig
import jhkim105.ssecompare.loadgen.SseClient
import jhkim105.ssecompare.loadgen.StageResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

private val logger = KotlinLogging.logger {}

object S5Drop {
    suspend fun run(
        client: SseClient,
        targetUrl: String,
        config: ScenarioConfig,
        resultWriter: ResultWriter,
    ): Pair<StageResult, RecoveryCheck> {
        val connections = config.connections.coerceAtLeast(1)
        val dropCount = (connections * config.dropRatio).toInt()
        val dropAfter = (config.events / 3).coerceAtLeast(1)
        logger.info { "[S5] Starting mid-stream disconnect: connections=$connections, drop=$dropCount after $dropAfter events" }

        val before = resultWriter.snapshotServerMetrics()
        val recorder = MetricsRecorder()
        val req = StreamRequest(
            prompt = "bench-S5-drop",
            events = config.events,
            intervalMs = config.intervalMs,
            persist = config.persist,
        )

        val startMs = System.currentTimeMillis()
        coroutineScope {
            val jobs = List(connections) { idx ->
                launch(Dispatchers.Default) {
                    val outcome = client.streamPost(
                        url = targetUrl,
                        request = req,
                        dropAfterEvents = if (idx < dropCount) dropAfter else null,
                    )
                    recorder.record(outcome)
                }
            }
            jobs.joinAll()
        }
        val durationMs = System.currentTimeMillis() - startMs
        val clientSnap = recorder.snapshot(durationMs)

        // Wait briefly for server cleanup + Prometheus scrape before recovery check
        delay(3_000L)
        val after = resultWriter.snapshotServerMetrics()
        val activeOk = (after.activeConnections ?: 0.0) <= (before.activeConnections ?: 0.0) + 1.0
        val threadsOk = before.liveThreads == null || after.liveThreads == null ||
            after.liveThreads <= before.liveThreads + 20.0
        val recovery = RecoveryCheck(
            beforeActiveConnections = before.activeConnections,
            afterActiveConnections = after.activeConnections,
            beforeThreads = before.liveThreads,
            afterThreads = after.liveThreads,
            recovered = activeOk && threadsOk,
        )

        val limit = LimitDetector.evaluate(clientSnap, config.intervalMs)
        return StageResult(
            stageName = "drop_c=$connections",
            targetConnections = connections,
            client = clientSnap,
            server = after,
            limitReached = limit.reached,
            limitReason = limit.reason,
        ) to recovery
    }
}
