package jhkim105.idgenerator

import org.junit.jupiter.api.Test
import kotlin.system.measureTimeMillis
import kotlin.test.assertTrue
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

class PerformanceComparisonTest {

    companion object {
        private const val ITERATIONS_SINGLE_THREAD = 1_000_000
        private const val ITERATIONS_MULTI_THREAD = 100_000
        private const val THREAD_COUNT = 10
        private const val WARMUP_ITERATIONS = 10_000
    }

    @Test
    fun `compare single-threaded throughput performance`() {
        println("[DEBUG_LOG] Starting single-threaded throughput comparison")
        
        // Warmup
        warmup()
        
        // Test TSID String generation
        val tsidStringTime = measureTimeMillis {
            repeat(ITERATIONS_SINGLE_THREAD) {
                TsidGenerator.generator<String>()
            }
        }
        
        // Test TSID Long generation
        val tsidLongTime = measureTimeMillis {
            repeat(ITERATIONS_SINGLE_THREAD) {
                TsidGenerator.generator<Long>()
            }
        }
        
        // Test UUID generation
        val uuidTime = measureTimeMillis {
            repeat(ITERATIONS_SINGLE_THREAD) {
                UuidGenerator.generate()
            }
        }
        
        val tsidStringThroughput = ITERATIONS_SINGLE_THREAD * 1000.0 / tsidStringTime
        val tsidLongThroughput = ITERATIONS_SINGLE_THREAD * 1000.0 / tsidLongTime
        val uuidThroughput = ITERATIONS_SINGLE_THREAD * 1000.0 / uuidTime
        
        println("[DEBUG_LOG] Single-threaded Results ($ITERATIONS_SINGLE_THREAD iterations):")
        println("[DEBUG_LOG] TSID String: ${tsidStringTime}ms, ${String.format("%.0f", tsidStringThroughput)} ops/sec")
        println("[DEBUG_LOG] TSID Long: ${tsidLongTime}ms, ${String.format("%.0f", tsidLongThroughput)} ops/sec")
        println("[DEBUG_LOG] UUID: ${uuidTime}ms, ${String.format("%.0f", uuidThroughput)} ops/sec")
        
        // Performance comparison analysis
        val fasterThanUuid = when {
            tsidStringThroughput > uuidThroughput -> "TSID String is ${String.format("%.2f", tsidStringThroughput / uuidThroughput)}x faster"
            uuidThroughput > tsidStringThroughput -> "UUID is ${String.format("%.2f", uuidThroughput / tsidStringThroughput)}x faster"
            else -> "TSID String and UUID have similar performance"
        }
        println("[DEBUG_LOG] $fasterThanUuid")
        
        // Ensure all generators are working
        assertTrue(tsidStringTime > 0)
        assertTrue(tsidLongTime > 0)
        assertTrue(uuidTime > 0)
    }

    @Test
    fun `compare multi-threaded throughput performance`() {
        println("[DEBUG_LOG] Starting multi-threaded throughput comparison")
        
        // Warmup
        warmup()
        
        val executor = Executors.newFixedThreadPool(THREAD_COUNT)
        
        // Test TSID String generation
        val tsidStringCounter = AtomicLong(0)
        val tsidStringTime = measureTimeMillis {
            val futures = (1..THREAD_COUNT).map {
                executor.submit {
                    repeat(ITERATIONS_MULTI_THREAD) {
                        TsidGenerator.generator<String>()
                        tsidStringCounter.incrementAndGet()
                    }
                }
            }
            futures.forEach { it.get() }
        }
        
        // Test TSID Long generation
        val tsidLongCounter = AtomicLong(0)
        val tsidLongTime = measureTimeMillis {
            val futures = (1..THREAD_COUNT).map {
                executor.submit {
                    repeat(ITERATIONS_MULTI_THREAD) {
                        TsidGenerator.generator<Long>()
                        tsidLongCounter.incrementAndGet()
                    }
                }
            }
            futures.forEach { it.get() }
        }
        
        // Test UUID generation
        val uuidCounter = AtomicLong(0)
        val uuidTime = measureTimeMillis {
            val futures = (1..THREAD_COUNT).map {
                executor.submit {
                    repeat(ITERATIONS_MULTI_THREAD) {
                        UuidGenerator.generate()
                        uuidCounter.incrementAndGet()
                    }
                }
            }
            futures.forEach { it.get() }
        }
        
        executor.shutdown()
        executor.awaitTermination(30, TimeUnit.SECONDS)
        
        val totalOperations = THREAD_COUNT * ITERATIONS_MULTI_THREAD
        val tsidStringThroughput = totalOperations * 1000.0 / tsidStringTime
        val tsidLongThroughput = totalOperations * 1000.0 / tsidLongTime
        val uuidThroughput = totalOperations * 1000.0 / uuidTime
        
        println("[DEBUG_LOG] Multi-threaded Results ($THREAD_COUNT threads, $ITERATIONS_MULTI_THREAD iterations each):")
        println("[DEBUG_LOG] TSID String: ${tsidStringTime}ms, ${String.format("%.0f", tsidStringThroughput)} ops/sec")
        println("[DEBUG_LOG] TSID Long: ${tsidLongTime}ms, ${String.format("%.0f", tsidLongThroughput)} ops/sec")
        println("[DEBUG_LOG] UUID: ${uuidTime}ms, ${String.format("%.0f", uuidThroughput)} ops/sec")
        
        // Performance comparison analysis
        val fasterThanUuid = when {
            tsidStringThroughput > uuidThroughput -> "TSID String is ${String.format("%.2f", tsidStringThroughput / uuidThroughput)}x faster"
            uuidThroughput > tsidStringThroughput -> "UUID is ${String.format("%.2f", uuidThroughput / tsidStringThroughput)}x faster"
            else -> "TSID String and UUID have similar performance"
        }
        println("[DEBUG_LOG] Multi-threaded: $fasterThanUuid")
        
        // Verify all operations completed
        assertTrue(tsidStringCounter.get() == totalOperations.toLong())
        assertTrue(tsidLongCounter.get() == totalOperations.toLong())
        assertTrue(uuidCounter.get() == totalOperations.toLong())
    }

    @Test
    fun `compare average latency per operation`() {
        println("[DEBUG_LOG] Starting latency comparison")
        
        // Warmup
        warmup()
        
        val iterations = 100_000
        
        // Measure TSID String latency
        val tsidStringLatencies = mutableListOf<Long>()
        repeat(iterations) {
            val startTime = System.nanoTime()
            TsidGenerator.generator<String>()
            val endTime = System.nanoTime()
            tsidStringLatencies.add(endTime - startTime)
        }
        
        // Measure TSID Long latency
        val tsidLongLatencies = mutableListOf<Long>()
        repeat(iterations) {
            val startTime = System.nanoTime()
            TsidGenerator.generator<Long>()
            val endTime = System.nanoTime()
            tsidLongLatencies.add(endTime - startTime)
        }
        
        // Measure UUID latency
        val uuidLatencies = mutableListOf<Long>()
        repeat(iterations) {
            val startTime = System.nanoTime()
            UuidGenerator.generate()
            val endTime = System.nanoTime()
            uuidLatencies.add(endTime - startTime)
        }
        
        val tsidStringAvgLatency = tsidStringLatencies.average()
        val tsidLongAvgLatency = tsidLongLatencies.average()
        val uuidAvgLatency = uuidLatencies.average()
        
        val tsidStringP95 = tsidStringLatencies.sorted()[((iterations * 0.95).toInt())]
        val tsidLongP95 = tsidLongLatencies.sorted()[((iterations * 0.95).toInt())]
        val uuidP95 = uuidLatencies.sorted()[((iterations * 0.95).toInt())]
        
        println("[DEBUG_LOG] Latency Results ($iterations iterations):")
        println("[DEBUG_LOG] TSID String - Avg: ${String.format("%.2f", tsidStringAvgLatency)}ns, P95: ${tsidStringP95}ns")
        println("[DEBUG_LOG] TSID Long - Avg: ${String.format("%.2f", tsidLongAvgLatency)}ns, P95: ${tsidLongP95}ns")
        println("[DEBUG_LOG] UUID - Avg: ${String.format("%.2f", uuidAvgLatency)}ns, P95: ${uuidP95}ns")
        
        // Performance comparison
        val latencyComparison = when {
            tsidStringAvgLatency < uuidAvgLatency -> "TSID String has ${String.format("%.2f", uuidAvgLatency / tsidStringAvgLatency)}x lower latency"
            uuidAvgLatency < tsidStringAvgLatency -> "UUID has ${String.format("%.2f", tsidStringAvgLatency / uuidAvgLatency)}x lower latency"
            else -> "TSID String and UUID have similar latency"
        }
        println("[DEBUG_LOG] $latencyComparison")
        
        // Ensure measurements are reasonable
        assertTrue(tsidStringAvgLatency > 0)
        assertTrue(tsidLongAvgLatency > 0)
        assertTrue(uuidAvgLatency > 0)
    }

    @Test
    fun `test uniqueness under high load`() {
        println("[DEBUG_LOG] Testing uniqueness under high load")
        
        val iterations = 100_000
        val tsidStringSet = mutableSetOf<String>()
        val tsidLongSet = mutableSetOf<Long>()
        val uuidSet = mutableSetOf<String>()
        
        // Test TSID String uniqueness
        val tsidStringTime = measureTimeMillis {
            repeat(iterations) {
                val id = TsidGenerator.generator<String>()
                tsidStringSet.add(id)
            }
        }
        
        // Test TSID Long uniqueness
        val tsidLongTime = measureTimeMillis {
            repeat(iterations) {
                val id = TsidGenerator.generator<Long>()
                tsidLongSet.add(id)
            }
        }
        
        // Test UUID uniqueness
        val uuidTime = measureTimeMillis {
            repeat(iterations) {
                val id = UuidGenerator.generate()
                uuidSet.add(id)
            }
        }
        
        println("[DEBUG_LOG] Uniqueness test results ($iterations iterations):")
        println("[DEBUG_LOG] TSID String: ${tsidStringSet.size} unique IDs in ${tsidStringTime}ms")
        println("[DEBUG_LOG] TSID Long: ${tsidLongSet.size} unique IDs in ${tsidLongTime}ms")
        println("[DEBUG_LOG] UUID: ${uuidSet.size} unique IDs in ${uuidTime}ms")
        
        // All IDs should be unique
        assertTrue(tsidStringSet.size == iterations, "TSID String should generate unique IDs")
        assertTrue(tsidLongSet.size == iterations, "TSID Long should generate unique IDs")
        assertTrue(uuidSet.size == iterations, "UUID should generate unique IDs")
    }

    private fun warmup() {
        println("[DEBUG_LOG] Warming up generators...")
        repeat(WARMUP_ITERATIONS) {
            TsidGenerator.generator<String>()
            TsidGenerator.generator<Long>()
            UuidGenerator.generate()
        }
        println("[DEBUG_LOG] Warmup completed")
    }
}