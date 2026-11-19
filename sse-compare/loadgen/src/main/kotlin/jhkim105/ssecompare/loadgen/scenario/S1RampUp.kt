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

object S1RampUp {
    suspend fun run(
        client: SseClient,
        targetUrl: String,
        config: ScenarioConfig,
        resultWriter: ResultWriter,
        overrideStages: List<Int>? = null,
    ): Pair<List<StageResult>, Int?> {
        val stages = overrideStages?.takeIf { it.isNotEmpty() } ?: config.stages
        val results = mutableListOf<StageResult>()
        var firstLimitStage: Int? = null

        for (concurrency in stages) {
            logger.info { "[${config.id}] Starting stage: concurrency=$concurrency, events=${config.events}, intervalMs=${config.intervalMs}, persist=${config.persist}" }
            val recorder = MetricsRecorder()
            val req = StreamRequest(
                prompt = "bench-${config.id}-c$concurrency",
                events = config.events,
                intervalMs = config.intervalMs,
                persist = config.persist,
            )

            val startMs = System.currentTimeMillis()
            coroutineScope {
                val jobs = List(concurrency) {
                    launch(Dispatchers.Default) {
                        val outcome = client.streamPost(targetUrl, req)
                        recorder.record(outcome)
                    }
                }
                jobs.joinAll()
            }
            val durationMs = System.currentTimeMillis() - startMs
            val clientSnap = recorder.snapshot(durationMs)
            val serverSnap = resultWriter.snapshotServerMetrics()
            val limit = LimitDetector.evaluate(clientSnap, config.intervalMs)

            if (limit.reached && firstLimitStage == null) {
                firstLimitStage = concurrency
            }

            logger.info {
                "[${config.id}] Stage c=$concurrency done in ${durationMs}ms | " +
                    "ttfbP95=${"%.1f".format(clientSnap.ttfbMs.p95)}ms, " +
                    "gapP99=${"%.1f".format(clientSnap.eventGapMs.p99)}ms, " +
                    "err=${"%.2f".format(clientSnap.errorRate * 100)}%, " +
                    "ev/s=${"%.1f".format(clientSnap.throughputEventsPerSec)}, limit=${limit.reached}"
            }

            results.add(
                StageResult(
                    stageName = "c=$concurrency",
                    targetConnections = concurrency,
                    client = clientSnap,
                    server = serverSnap,
                    limitReached = limit.reached,
                    limitReason = limit.reason,
                ),
            )
        }
        return results to firstLimitStage
    }
}
