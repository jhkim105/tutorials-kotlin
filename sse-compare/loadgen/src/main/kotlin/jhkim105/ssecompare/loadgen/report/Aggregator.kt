package jhkim105.ssecompare.loadgen.report

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.oshai.kotlinlogging.KotlinLogging
import jhkim105.ssecompare.loadgen.BenchmarkRunResult
import java.io.File
import kotlin.math.abs

private val logger = KotlinLogging.logger {}

data class AggregatedRow(
    val combination: String,
    val scenario: String,
    val stageName: String,
    val runs: Int,
    val medianTtfbP95: Double,
    val medianGapP50: Double,
    val medianGapP95: Double,
    val medianGapP99: Double,
    val medianErrorRatePct: Double,
    val medianThroughputEvPerSec: Double,
    val maxP95DeviationPct: Double,
    val maxThroughputDeviationPct: Double,
    val deviationWarning: Boolean,
    val limitReached: Boolean,
    val medianThreads: Double?,
    val medianHeapMb: Double?,
)

object Aggregator {
    private val objectMapper = jacksonObjectMapper()

    fun aggregate(resultsDir: File): List<AggregatedRow> {
        val jsonFiles = resultsDir.walkTopDown()
            .filter { it.isFile && it.extension == "json" && it.name != "combinations.json" }
            .toList()

        if (jsonFiles.isEmpty()) {
            logger.warn { "No benchmark result JSON files found under ${resultsDir.absolutePath}" }
            return emptyList()
        }

        val runs = jsonFiles.mapNotNull { file ->
            runCatching { objectMapper.readValue<BenchmarkRunResult>(file) }
                .onFailure { logger.warn { "Skipping unreadable result file ${file.name}: ${it.message}" } }
                .getOrNull()
        }

        data class GroupKey(val combo: String, val scenario: String, val stage: String)

        val grouped = runs.flatMap { run ->
            run.stages.map { st -> GroupKey(run.combination, run.scenario, st.stageName) to st }
        }.groupBy({ it.first }, { it.second })

        return grouped.entries
            .sortedWith(compareBy({ it.key.scenario }, { it.key.combo }, { it.value.first().targetConnections }))
            .map { (key, stages) ->
                val ttfbP95s = stages.map { it.client.ttfbMs.p95 }
                val gapP50s = stages.map { it.client.eventGapMs.p50 }
                val gapP95s = stages.map { it.client.eventGapMs.p95 }
                val gapP99s = stages.map { it.client.eventGapMs.p99 }
                val errPcts = stages.map { it.client.errorRate * 100.0 }
                val tps = stages.map { it.client.throughputEventsPerSec }
                val threads = stages.mapNotNull { it.server.liveThreads }
                val heaps = stages.mapNotNull { it.server.heapMb }

                val medP95 = median(gapP95s)
                val medTp = median(tps)
                val p95Dev = maxDeviationPct(gapP95s, medP95)
                val tpDev = maxDeviationPct(tps, medTp)

                AggregatedRow(
                    combination = key.combo,
                    scenario = key.scenario,
                    stageName = key.stage,
                    runs = stages.size,
                    medianTtfbP95 = median(ttfbP95s),
                    medianGapP50 = median(gapP50s),
                    medianGapP95 = medP95,
                    medianGapP99 = median(gapP99s),
                    medianErrorRatePct = median(errPcts),
                    medianThroughputEvPerSec = medTp,
                    maxP95DeviationPct = p95Dev,
                    maxThroughputDeviationPct = tpDev,
                    deviationWarning = stages.size >= 2 && (p95Dev > 15.0 || tpDev > 15.0),
                    limitReached = stages.any { it.limitReached },
                    medianThreads = if (threads.isEmpty()) null else median(threads),
                    medianHeapMb = if (heaps.isEmpty()) null else median(heaps),
                )
            }
    }

    fun writeCsvAndReport(resultsDir: File) {
        val rows = aggregate(resultsDir)
        val csvFile = File(resultsDir, "summary.csv")
        csvFile.writeText(buildCsv(rows))
        logger.info { "Wrote CSV summary to ${csvFile.absolutePath}" }

        val mdFile = File(resultsDir, "RESULTS.md")
        mdFile.writeText(buildMarkdown(rows))
        logger.info { "Wrote Markdown report to ${mdFile.absolutePath}" }
    }

    private fun buildCsv(rows: List<AggregatedRow>): String = buildString {
        appendLine("combination,scenario,stage,runs,ttfb_p95_ms,gap_p50_ms,gap_p95_ms,gap_p99_ms,error_rate_pct,events_per_sec,p95_dev_pct,tp_dev_pct,dev_warning,limit_reached,threads,heap_mb")
        rows.forEach { r ->
            appendLine(
                listOf(
                    r.combination,
                    r.scenario,
                    r.stageName,
                    r.runs,
                    "%.1f".format(r.medianTtfbP95),
                    "%.1f".format(r.medianGapP50),
                    "%.1f".format(r.medianGapP95),
                    "%.1f".format(r.medianGapP99),
                    "%.2f".format(r.medianErrorRatePct),
                    "%.1f".format(r.medianThroughputEvPerSec),
                    "%.1f".format(r.maxP95DeviationPct),
                    "%.1f".format(r.maxThroughputDeviationPct),
                    r.deviationWarning,
                    r.limitReached,
                    r.medianThreads?.let { "%.0f".format(it) } ?: "",
                    r.medianHeapMb?.let { "%.1f".format(it) } ?: "",
                ).joinToString(","),
            )
        }
    }

    private fun buildMarkdown(rows: List<AggregatedRow>): String = buildString {
        appendLine("# SSE Compare 벤치마크 결과 보고서")
        appendLine()
        appendLine("## 1. 실행 환경 및 공통 설정 (FR-014, FR-018)")
        appendLine()
        appendLine("| 항목 | 설정값 |")
        appendLine("|------|--------|")
        appendLine("| 런타임 | Java 21, Kotlin 1.9.25, Spring Boot 3.4.4 |")
        appendLine("| 컨테이너 제한 | `cpus: 2.0`, `memory: 1g` |")
        appendLine("| JVM 옵션 | `-Xmx512m -Xms512m -XX:+UseG1GC` |")
        appendLine("| Tomcat 설정 (MVC) | `threads.max: 200`, `max-connections: 20000`, `accept-count: 1000` |")
        appendLine("| DB 커넥션 풀 (S4) | HikariCP `maximum-pool-size: 10`, R2DBC `max-size: 10` (획득 타임아웃 5s) |")
        appendLine("| 한계 판정 기준 (SC-003) | 에러율 > 1% 또는 이벤트 간 지연 p99 > 2 × `intervalMs` |")
        appendLine("| 해석 주의 | 로컬 macOS Docker Desktop 결과는 절대 수치가 아닌 **구성 간 상대 비교용** |")
        appendLine()
        appendLine("## 2. 시나리오별 측정 비교표")
        appendLine()
        if (rows.isEmpty()) {
            appendLine("_아직 수집된 벤치마크 결과가 없습니다. `./scripts/run-benchmark.sh <combo> <scenario>` 실행 후 `--report`로 재생성하세요._")
        } else {
            appendLine("| 시나리오 | 구성 | 단계 | 반복 | TTFB p95(ms) | Gap p50/p95/p99(ms) | 에러율(%) | 처리량(ev/s) | 편차 경고(±15%) | 한계 | 스레드 | Heap(MB) |")
            appendLine("|----------|------|------|------|--------------|---------------------|-----------|--------------|-----------------|------|--------|----------|")
            rows.forEach { r ->
                val warn = if (r.deviationWarning) "⚠️ p95:${"%.1f".format(r.maxP95DeviationPct)}% tp:${"%.1f".format(r.maxThroughputDeviationPct)}%" else "OK"
                val lim = if (r.limitReached) "LIMIT" else "-"
                appendLine(
                    "| ${r.scenario} | ${r.combination} | ${r.stageName} | ${r.runs} | " +
                        "${"%.1f".format(r.medianTtfbP95)} | " +
                        "${"%.1f".format(r.medianGapP50)} / ${"%.1f".format(r.medianGapP95)} / ${"%.1f".format(r.medianGapP99)} | " +
                        "${"%.2f".format(r.medianErrorRatePct)} | " +
                        "${"%.1f".format(r.medianThroughputEvPerSec)} | $warn | $lim | " +
                        "${r.medianThreads?.let { "%.0f".format(it) } ?: "-"} | " +
                        "${r.medianHeapMb?.let { "%.1f".format(it) } ?: "-"} |",
                )
            }
        }
        appendLine()
        appendLine("## 3. 워크로드별 실서비스 추천 (SC-008)")
        appendLine()
        appendLine("| 워크로드 유형 | 추천 구성 | 근거 및 트레이드오프 |")
        appendLine("|---------------|-----------|----------------------|")
        appendLine("| **기존 MVC + JPA 블로킹 서비스** | **B (MVC + SseEmitter + Virtual Thread)** | `spring.threads.virtual.enabled=true` 설정만으로 플랫폼 스레드 200개 한계를 돌파한다. 코드 변경·마이그레이션 비용이 가장 낮으며, DB 병목은 HikariCP 풀 크기로 명확히 통제된다. |")
        appendLine("| **신규 I/O 중심 서비스 (LLM 프록시 등)** | **E (WebFlux + Coroutine Flow)** | Netty 논블로킹 이벤트 루프와 Kotlin `suspend`/`Flow` 조합으로 연결당 메모리와 스레드 오버헤드가 가장 적고, 느린 클라이언트 백프레셔가 자연스럽다. 단, 블로킹 라이브러리 혼용 시 격리(`Dispatchers.IO`) 또는 R2DBC 전환이 필요하다. |")
        appendLine("| **MVC 유지 + 코루틴 스트림 문법 선호** | **C2 (MVC + Flow + VT Dispatcher)** | 기존 서블릿 필터/보안/JPA 생태계를 유지하면서 코루틴 `Flow` 문법을 쓸 수 있다. 다만 쓰기 시점에는 서블릿 스레드를 거치므로 초대규모 동시 연결에서는 WebFlux(E)보다 불리하다. |")
        appendLine("| **팀이 Reactor에 익숙한 리액티브 서비스** | **D (WebFlux + Reactor Flux)** | E와 동등한 Netty 논블로킹 확장성을 제공하지만, 코루틴 대비 코드 가독성과 디버깅 학습 비용이 높다. |")
    }

    internal fun median(values: List<Double>): Double {
        if (values.isEmpty()) return 0.0
        val sorted = values.sorted()
        val mid = sorted.size / 2
        return if (sorted.size % 2 == 0) (sorted[mid - 1] + sorted[mid]) / 2.0 else sorted[mid]
    }

    internal fun maxDeviationPct(values: List<Double>, center: Double): Double {
        if (values.size < 2 || center == 0.0) return 0.0
        return values.maxOf { abs(it - center) / abs(center) * 100.0 }
    }
}
