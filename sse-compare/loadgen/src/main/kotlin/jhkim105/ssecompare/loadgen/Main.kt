package jhkim105.ssecompare.loadgen

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.oshai.kotlinlogging.KotlinLogging
import jhkim105.ssecompare.common.StreamRequest
import jhkim105.ssecompare.loadgen.report.Aggregator
import jhkim105.ssecompare.loadgen.scenario.S1RampUp
import jhkim105.ssecompare.loadgen.scenario.S2ShortStreams
import jhkim105.ssecompare.loadgen.scenario.S3LongHold
import jhkim105.ssecompare.loadgen.scenario.S4WithDb
import jhkim105.ssecompare.loadgen.scenario.S5Drop
import jhkim105.ssecompare.loadgen.scenario.S6SlowClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.time.Instant

private val logger = KotlinLogging.logger {}

fun main(args: Array<String>) {
    val parsed = parseArgs(args)
    val mode = parsed["mode"] ?: "run"

    if (mode == "report") {
        val resultsDir = File(parsed["results-dir"] ?: "results")
        Aggregator.writeCsvAndReport(resultsDir)
        return
    }

    val combo = parsed["combo"] ?: error("--combo is required (e.g. A, B, C1, C2, D, E)")
    val scenarioId = parsed["scenario"] ?: error("--scenario is required (S1..S6)")
    val baseUrl = (parsed["url"] ?: "http://localhost:8081").trimEnd('/')
    val targetUrl = "$baseUrl/chat/stream"
    val prometheusUrl = parsed["prometheus"]
    val prometheusJob = parsed["prometheus-job"]
    val runIndex = parsed["run"]?.toInt() ?: 1
    val warmupConns = parsed["warmup"]?.toInt() ?: 20
    val outDir = File(parsed["out"] ?: "results")
    val overrideStages = parsed["stages"]?.split(",")?.mapNotNull { it.trim().toIntOrNull() }

    val objectMapper = jacksonObjectMapper()
    val scenariosStream = object {}.javaClass.getResourceAsStream("/scenarios.json")
        ?: error("scenarios.json not found on classpath")
    val scenarios: List<ScenarioConfig> = objectMapper.readValue(scenariosStream)
    val scenario = scenarios.find { it.id.equals(scenarioId, ignoreCase = true) }
        ?: error("Unknown scenario: $scenarioId")

    val resultWriter = ResultWriter(prometheusUrl, prometheusJob)

    runBlocking {
        SseClient(maxConnections = 20_000).use { client ->
            if (warmupConns > 0) {
                logger.info { "Warming up $combo ($targetUrl) with $warmupConns streams..." }
                val warmupReq = StreamRequest(prompt = "warmup", events = 3, intervalMs = 50, persist = false)
                coroutineScope {
                    List(warmupConns) {
                        launch(Dispatchers.Default) {
                            client.streamPost(targetUrl, warmupReq)
                        }
                    }.joinAll()
                }
            }

            val stages = mutableListOf<StageResult>()
            var limitStage: Int? = null
            var recovery: RecoveryCheck? = null
            var trendSamples: List<ServerMetricsSnapshot> = emptyList()

            when (scenario.id.uppercase()) {
                "S1" -> {
                    val (st, lim) = S1RampUp.run(client, targetUrl, scenario, resultWriter, overrideStages)
                    stages.addAll(st)
                    limitStage = lim
                }
                "S2" -> stages.add(S2ShortStreams.run(client, targetUrl, scenario, resultWriter))
                "S3" -> {
                    val (st, samples) = S3LongHold.run(client, targetUrl, scenario, resultWriter)
                    stages.add(st)
                    trendSamples = samples
                }
                "S4" -> {
                    val (st, lim) = S4WithDb.run(client, targetUrl, scenario, resultWriter, overrideStages)
                    stages.addAll(st)
                    limitStage = lim
                }
                "S5" -> {
                    val (st, rec) = S5Drop.run(client, targetUrl, scenario, resultWriter)
                    stages.add(st)
                    recovery = rec
                }
                "S6" -> stages.add(S6SlowClient.run(client, targetUrl, scenario, resultWriter))
                else -> error("Unsupported scenario: ${scenario.id}")
            }

            val result = BenchmarkRunResult(
                combination = combo,
                scenario = scenario.id.uppercase(),
                run = runIndex,
                timestamp = Instant.now().toString(),
                targetUrl = targetUrl,
                stages = stages,
                limitStage = limitStage,
                recovery = recovery,
                trendSamples = trendSamples,
            )
            resultWriter.writeResult(outDir, result)
        }
    }
}

private fun parseArgs(args: Array<String>): Map<String, String> {
    val map = mutableMapOf<String, String>()
    for (arg in args) {
        if (arg.startsWith("--") && arg.contains("=")) {
            val idx = arg.indexOf('=')
            map[arg.substring(2, idx)] = arg.substring(idx + 1)
        }
    }
    return map
}
