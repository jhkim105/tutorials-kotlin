package jhkim105.ssecompare.loadgen

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.ceil

class MetricsRecorder {
    private val ttfbSamples = ConcurrentLinkedQueue<Long>()
    private val eventGapSamples = ConcurrentLinkedQueue<Long>()
    private val totalStreams = AtomicInteger(0)
    private val completedStreams = AtomicInteger(0)
    private val failedStreams = AtomicInteger(0)
    private val totalEvents = AtomicLong(0)

    fun record(outcome: StreamOutcome) {
        totalStreams.incrementAndGet()
        if (outcome.success) {
            completedStreams.incrementAndGet()
        } else {
            failedStreams.incrementAndGet()
        }
        outcome.ttfbMs?.let { ttfbSamples.add(it) }
        outcome.eventGapsMs.forEach { eventGapSamples.add(it) }
        totalEvents.addAndGet(outcome.eventsReceived.toLong())
    }

    fun snapshot(durationMs: Long): ClientMetricsSnapshot {
        val total = totalStreams.get()
        val completed = completedStreams.get()
        val failed = failedStreams.get()
        val events = totalEvents.get()
        val durationSec = (durationMs.coerceAtLeast(1L)) / 1000.0

        return ClientMetricsSnapshot(
            ttfbMs = computePercentiles(ttfbSamples.toList()),
            eventGapMs = computePercentiles(eventGapSamples.toList()),
            errorRate = if (total == 0) 0.0 else failed.toDouble() / total.toDouble(),
            throughputEventsPerSec = events / durationSec,
            throughputStreamsPerSec = completed / durationSec,
            completionRate = if (total == 0) 0.0 else completed.toDouble() / total.toDouble(),
            totalStreams = total,
            completedStreams = completed,
            failedStreams = failed,
            totalEvents = events,
            durationMs = durationMs,
        )
    }

    companion object {
        fun computePercentiles(values: List<Long>): Percentiles {
            if (values.isEmpty()) {
                return Percentiles(0.0, 0.0, 0.0, 0.0, 0.0)
            }
            val sorted = values.sorted()
            return Percentiles(
                p50 = percentile(sorted, 0.50),
                p95 = percentile(sorted, 0.95),
                p99 = percentile(sorted, 0.99),
                avg = sorted.average(),
                max = sorted.last().toDouble(),
            )
        }

        private fun percentile(sorted: List<Long>, p: Double): Double {
            val idx = (ceil(p * sorted.size).toInt() - 1).coerceIn(0, sorted.lastIndex)
            return sorted[idx].toDouble()
        }
    }
}
