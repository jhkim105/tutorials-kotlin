package jhkim105.idgenerator

import com.github.f4b6a3.tsid.Tsid
import com.github.f4b6a3.tsid.TsidCreator
import org.junit.jupiter.api.Test
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import kotlin.system.measureNanoTime

/**
 * Tsid.fast() vs TsidCreator.getTsid() 성능 비교 벤치마크 테스트
 *
 * ┌─────────────────────────┬──────────────────────────────┬──────────────────────────┐
 * │                         │ TsidCreator.getTsid()        │ Tsid.fast()              │
 * ├─────────────────────────┼──────────────────────────────┼──────────────────────────┤
 * │ 내부 구현               │ TsidFactory (synchronized)   │ currentTimeMillis+random │
 * │ 스레드 안전성           │ ✅ (lock 사용)               │ ⚠️ (lock-free, 충돌 가능)│
 * │ 단조 증가(monotonic)    │ ✅ 보장                      │ ❌ 미보장                │
 * │ Sequence 관리           │ ✅ ms 당 카운터              │ ❌ 없음                  │
 * │ 멀티스레드 경합         │ synchronized 로 인한 경합    │ 경합 없음                │
 * │ 예상 성능               │ 상대적으로 느림              │ 더 빠름                  │
 * └─────────────────────────┴──────────────────────────────┴──────────────────────────┘
 *
 * 언제 무엇을 써야 하나?
 *  - TsidCreator.getTsid() : DB PK, 분산 시스템 ID 등 충돌이 절대 없어야 하는 경우
 *  - Tsid.fast()           : 로그 트레이싱, 캐시 키 등 고성능이 필요하고 중복을 감수할 수 있는 경우
 */
class TsidFastVsGetTsidBenchmarkTest {

    companion object {
        private const val WARMUP_ITERATIONS = 50_000
        private const val SINGLE_THREAD_ITERATIONS = 1_000_000
        private const val MULTI_THREAD_ITERATIONS = 200_000
        private const val THREAD_COUNT = 8
    }

    // ──────────────────────────────────────────────────────────────────────
    // 1. 단일 스레드 처리량(Throughput) 비교
    // ──────────────────────────────────────────────────────────────────────

    @Test
    fun `single thread - getTsid vs fast throughput`() {
        println("\n========== [단일 스레드] 처리량(Throughput) 비교 ==========")
        warmup()

        // --- TsidCreator.getTsid() ---
        val getTsidNanos = measureNanoTime {
            repeat(SINGLE_THREAD_ITERATIONS) {
                TsidCreator.getTsid()
            }
        }

        // --- Tsid.fast() ---
        val fastNanos = measureNanoTime {
            repeat(SINGLE_THREAD_ITERATIONS) {
                Tsid.fast()
            }
        }

        printThroughputResult(
            label1 = "TsidCreator.getTsid()",
            nanos1 = getTsidNanos,
            label2 = "Tsid.fast()",
            nanos2 = fastNanos,
            iterations = SINGLE_THREAD_ITERATIONS
        )
    }

    // ──────────────────────────────────────────────────────────────────────
    // 2. 단일 스레드 Latency(ns 단위) 비교
    // ──────────────────────────────────────────────────────────────────────

    @Test
    fun `single thread - getTsid vs fast latency ns`() {
        println("\n========== [단일 스레드] 레이턴시(Latency) 비교 ==========")
        warmup()

        val sampleSize = 100_000

        val getTsidLatencies = LongArray(sampleSize) { measureNanoTime { TsidCreator.getTsid() } }
        val fastLatencies = LongArray(sampleSize) { measureNanoTime { Tsid.fast() } }

        printLatencyStats("TsidCreator.getTsid()", getTsidLatencies)
        printLatencyStats("Tsid.fast()", fastLatencies)

        val avgGetTsid = getTsidLatencies.average()
        val avgFast = fastLatencies.average()
        val ratio = if (avgGetTsid > avgFast) avgGetTsid / avgFast else avgFast / avgGetTsid
        val faster = if (avgFast < avgGetTsid) "Tsid.fast()" else "TsidCreator.getTsid()"
        println("\n  → $faster 이 평균 레이턴시 기준으로 약 ${"%.2f".format(ratio)}배 빠름")
    }

    // ──────────────────────────────────────────────────────────────────────
    // 3. 멀티 스레드 처리량 비교 (경합 상황)
    // ──────────────────────────────────────────────────────────────────────

    @Test
    fun `multi thread - getTsid vs fast throughput`() {
        println("\n========== [멀티 스레드 $THREAD_COUNT 개] 처리량(Throughput) 비교 ==========")
        warmup()

        // --- TsidCreator.getTsid() ---
        val getTsidNanos = runMultiThreaded { TsidCreator.getTsid() }

        // --- Tsid.fast() ---
        val fastNanos = runMultiThreaded { Tsid.fast() }

        val totalIterations = THREAD_COUNT * MULTI_THREAD_ITERATIONS
        printThroughputResult(
            label1 = "TsidCreator.getTsid()",
            nanos1 = getTsidNanos,
            label2 = "Tsid.fast()",
            nanos2 = fastNanos,
            iterations = totalIterations
        )
    }

    // ──────────────────────────────────────────────────────────────────────
    // 4. 멀티 스레드 중복 발생 여부 확인 (uniqueness)
    // ──────────────────────────────────────────────────────────────────────

    @Test
    fun `multi thread - uniqueness check getTsid vs fast`() {
        println("\n========== [유일성] 멀티 스레드 중복 발생 여부 확인 ==========")

        val iterations = 500_000
        val threadCount = 8

        // TsidCreator.getTsid() 유일성 확인
        val getTsidSet = ConcurrentHashMap.newKeySet<Long>()
        val getTsidDuplicates = AtomicLong(0)
        runMultiThreaded(threadCount, iterations / threadCount) {
            val id = TsidCreator.getTsid().toLong()
            if (!getTsidSet.add(id)) getTsidDuplicates.incrementAndGet()
        }

        // Tsid.fast() 유일성 확인
        val fastSet = ConcurrentHashMap.newKeySet<Long>()
        val fastDuplicates = AtomicLong(0)
        runMultiThreaded(threadCount, iterations / threadCount) {
            val id = Tsid.fast().toLong()
            if (!fastSet.add(id)) fastDuplicates.incrementAndGet()
        }

        println("  TsidCreator.getTsid() -> 총 ${iterations}개 중 중복: ${getTsidDuplicates.get()}개")
        println("  Tsid.fast()           -> 총 ${iterations}개 중 중복: ${fastDuplicates.get()}개")

        if (fastDuplicates.get() > 0) {
            println("\n  [경고] Tsid.fast() 는 멀티스레드 환경에서 중복이 발생할 수 있습니다!")
            println("       -> 충돌이 허용되지 않는 용도(DB PK 등)에는 TsidCreator.getTsid() 를 사용하세요.")
        } else {
            println("\n  [OK] 이번 실행에서는 두 방법 모두 중복 없음. (fast()는 확률적으로 중복 발생 가능)")
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // 5. 단조 증가(Monotonic) 보장 여부 확인
    // ──────────────────────────────────────────────────────────────────────

    @Test
    fun `monotonic increase check - getTsid vs fast`() {
        println("\n========== [단조 증가] 확인 ==========")

        val sampleSize = 10_000

        // TsidCreator.getTsid() 단조 증가 확인 (LongArray -> List 변환 후 zipWithNext 사용)
        val getTsidList = LongArray(sampleSize) { TsidCreator.getTsid().toLong() }.toList()
        val getTsidViolations = getTsidList.zipWithNext().count { (a, b) -> b <= a }

        // Tsid.fast() 단조 증가 확인 (LongArray -> List 변환 후 zipWithNext 사용)
        val fastList = LongArray(sampleSize) { Tsid.fast().toLong() }.toList()
        val fastViolations = fastList.zipWithNext().count { (a, b) -> b <= a }

        println("  TsidCreator.getTsid() -> 단조 증가 위반 횟수: $getTsidViolations / ${sampleSize - 1}")
        println("  Tsid.fast()           -> 단조 증가 위반 횟수: $fastViolations / ${sampleSize - 1}")

        println("\n  -> TsidCreator.getTsid() 는 단조 증가 ${if (getTsidViolations == 0) "[보장]" else "[위반 발생]"}")
        println("  -> Tsid.fast()           는 단조 증가 ${if (fastViolations == 0) "[이번엔 위반 없음]" else "[위반 ${fastViolations}건 발생]"}")
        println("     fast() 의 단조 증가는 보장되지 않습니다. (랜덤 비트 사용)")
    }

    // ──────────────────────────────────────────────────────────────────────
    // Helper: warmup
    // ──────────────────────────────────────────────────────────────────────

    private fun warmup() {
        print("  워밍업 중($WARMUP_ITERATIONS 회)...")
        repeat(WARMUP_ITERATIONS) {
            TsidCreator.getTsid()
            Tsid.fast()
        }
        println(" 완료")
    }

    // ──────────────────────────────────────────────────────────────────────
    // Helper: 멀티스레드 실행 (나노초 반환)
    // ──────────────────────────────────────────────────────────────────────

    private fun runMultiThreaded(block: () -> Unit): Long {
        return runMultiThreaded(THREAD_COUNT, MULTI_THREAD_ITERATIONS, block)
    }

    private fun runMultiThreaded(threadCount: Int, iterationsPerThread: Int, block: () -> Unit): Long {
        val executor = Executors.newFixedThreadPool(threadCount)
        val nanos = measureNanoTime {
            val futures = (1..threadCount).map {
                executor.submit {
                    repeat(iterationsPerThread) { block() }
                }
            }
            futures.forEach { it.get() }
        }
        executor.shutdown()
        executor.awaitTermination(30, TimeUnit.SECONDS)
        return nanos
    }

    // ──────────────────────────────────────────────────────────────────────
    // Helper: 처리량 결과 출력
    // ──────────────────────────────────────────────────────────────────────

    private fun printThroughputResult(
        label1: String, nanos1: Long,
        label2: String, nanos2: Long,
        iterations: Int
    ) {
        val throughput1 = iterations * 1_000_000_000.0 / nanos1
        val throughput2 = iterations * 1_000_000_000.0 / nanos2
        val avgNs1 = nanos1.toDouble() / iterations
        val avgNs2 = nanos2.toDouble() / iterations

        println("\n  $label1")
        println("    총 시간 : ${"%.3f".format(nanos1 / 1_000_000.0)} ms")
        println("    처리량  : ${"%.0f".format(throughput1)} ops/sec")
        println("    평균    : ${"%.2f".format(avgNs1)} ns/op")

        println("\n  $label2")
        println("    총 시간 : ${"%.3f".format(nanos2 / 1_000_000.0)} ms")
        println("    처리량  : ${"%.0f".format(throughput2)} ops/sec")
        println("    평균    : ${"%.2f".format(avgNs2)} ns/op")

        val ratio = if (throughput2 > throughput1) throughput2 / throughput1 else throughput1 / throughput2
        val faster = if (throughput2 > throughput1) label2 else label1
        println("\n  -> $faster 이 처리량 기준으로 약 ${"%.2f".format(ratio)}배 빠름")
    }

    // ──────────────────────────────────────────────────────────────────────
    // Helper: 레이턴시 통계 출력
    // ──────────────────────────────────────────────────────────────────────

    private fun printLatencyStats(label: String, latencies: LongArray) {
        val sorted = latencies.sorted()
        val avg = sorted.average()
        val p50 = sorted[(sorted.size * 0.50).toInt()]
        val p95 = sorted[(sorted.size * 0.95).toInt()]
        val p99 = sorted[(sorted.size * 0.99).toInt()]
        val max = sorted.last()

        println("\n  $label (${latencies.size}회 측정)")
        println("    Avg : ${"%.2f".format(avg)} ns")
        println("    P50 : $p50 ns")
        println("    P95 : $p95 ns")
        println("    P99 : $p99 ns")
        println("    Max : $max ns")
    }
}
