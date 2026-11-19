package jhkim105.ssecompare.loadgen

data class LimitCheck(
    val reached: Boolean,
    val reason: String? = null,
)

object LimitDetector {
    /**
     * SC-003: Limit is reached if errorRate > 1% OR eventGapMs.p99 > 2 * intervalMs.
     */
    fun evaluate(metrics: ClientMetricsSnapshot, intervalMs: Long): LimitCheck {
        val reasons = mutableListOf<String>()
        if (metrics.errorRate > 0.01) {
            reasons.add("errorRate=${"%.2f".format(metrics.errorRate * 100)}% > 1%")
        }
        if (intervalMs > 0 && metrics.eventGapMs.p99 > intervalMs * 2.0) {
            reasons.add("eventGapP99=${"%.1f".format(metrics.eventGapMs.p99)}ms > ${intervalMs * 2}ms")
        }
        return LimitCheck(
            reached = reasons.isNotEmpty(),
            reason = if (reasons.isEmpty()) null else reasons.joinToString("; "),
        )
    }
}
