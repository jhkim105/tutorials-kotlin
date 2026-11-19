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
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

private val logger = KotlinLogging.logger {}

object S2ShortStreams {
    suspend fun run(
        client: SseClient,
        targetUrl: String,
        config: ScenarioConfig,
        resultWriter: ResultWriter,
    ): StageResult {
        val rate = config.arrivalRatePerSec.coerceAtLeast(1)
        val durationSec = config.durationSeconds.coerceAtLeast(1)
        logger.info { "[S2] Starting fixed arrival rate: $rate req/s for ${durationSec}s (events=${config.events}, intervalMs=${config.intervalMs})" }

        val recorder = MetricsRecorder()
        val req = StreamRequest(
            prompt = "bench-S2",
            events = config.events,
            intervalMs = config.intervalMs,
            persist = config.persist,
        )

        val startMs = System.currentTimeMillis()
        coroutineScope {
            val jobs = mutableListOf<Job>()
            for (sec in 0 until durationSec) {
                val secStart = System.currentTimeMillis()
                repeat(rate) {
                    jobs.add(
                        launch(Dispatchers.Default) {
                            val outcome = client.streamPost(targetUrl, req)
                            recorder.record(outcome)
                        },
                    )
                }
                val elapsed = System.currentTimeMillis() - secStart
                if (elapsed < 1000L && sec < durationSec - 1) {
                    delay(1000L - elapsed)
                }
            }
            jobs.joinAll()
        }
        val durationMs = System.currentTimeMillis() - startMs
        val clientSnap = recorder.snapshot(durationMs)
        val serverSnap = resultWriter.snapshotServerMetrics()
        val limit = LimitDetector.evaluate(clientSnap, config.intervalMs)

        logger.info {
            "[S2] Done in ${durationMs}ms | streams=${clientSnap.completedStreams}/${clientSnap.totalStreams}, " +
                "ev/s=${"%.1f".format(clientSnap.throughputEventsPerSec)}, " +
                "err=${"%.2f".format(clientSnap.errorRate * 100)}%"
        }

        return StageResult(
            stageName = "rate=${rate}rps_${durationSec}s",
            targetConnections = rate,
            client = clientSnap,
            server = serverSnap,
            limitReached = limit.reached,
            limitReason = limit.reason,
        )
    }
}
