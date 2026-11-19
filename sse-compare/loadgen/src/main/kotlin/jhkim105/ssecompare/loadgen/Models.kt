package jhkim105.ssecompare.loadgen

data class ScenarioConfig(
    val id: String,
    val name: String,
    val mode: String,
    val stages: List<Int> = emptyList(),
    val connections: Int = 0,
    val arrivalRatePerSec: Int = 0,
    val durationSeconds: Int = 0,
    val events: Int = 10,
    val intervalMs: Long = 100,
    val holdSeconds: Int = 15,
    val persist: Boolean = false,
    val dropRatio: Double = 0.0,
    val readDelayMs: Long = 0,
)

data class Percentiles(
    val p50: Double,
    val p95: Double,
    val p99: Double,
    val avg: Double,
    val max: Double,
)

data class ClientMetricsSnapshot(
    val ttfbMs: Percentiles,
    val eventGapMs: Percentiles,
    val errorRate: Double,
    val throughputEventsPerSec: Double,
    val throughputStreamsPerSec: Double,
    val completionRate: Double,
    val totalStreams: Int,
    val completedStreams: Int,
    val failedStreams: Int,
    val totalEvents: Long,
    val durationMs: Long,
)

data class ServerMetricsSnapshot(
    val activeConnections: Double? = null,
    val liveThreads: Double? = null,
    val tomcatBusyThreads: Double? = null,
    val cpuPct: Double? = null,
    val heapMb: Double? = null,
    val gcPauseSecPerSec: Double? = null,
    val hikariActive: Double? = null,
    val hikariPending: Double? = null,
    val r2dbcAcquired: Double? = null,
)

data class StageResult(
    val stageName: String,
    val targetConnections: Int,
    val client: ClientMetricsSnapshot,
    val server: ServerMetricsSnapshot,
    val limitReached: Boolean,
    val limitReason: String? = null,
)

data class RecoveryCheck(
    val beforeActiveConnections: Double?,
    val afterActiveConnections: Double?,
    val beforeThreads: Double?,
    val afterThreads: Double?,
    val recovered: Boolean,
)

data class BenchmarkRunResult(
    val combination: String,
    val scenario: String,
    val run: Int,
    val timestamp: String,
    val targetUrl: String,
    val stages: List<StageResult>,
    val limitStage: Int? = null,
    val recovery: RecoveryCheck? = null,
    val trendSamples: List<ServerMetricsSnapshot> = emptyList(),
)
