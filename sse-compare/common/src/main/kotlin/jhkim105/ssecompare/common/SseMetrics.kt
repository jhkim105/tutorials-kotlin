package jhkim105.ssecompare.common

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class SseMetrics(meterRegistry: MeterRegistry) {
    private val activeConnectionsCount = AtomicInteger(0)

    private val eventsSentCounter: Counter = Counter.builder("sse.events.sent")
        .description("Total SSE message events sent")
        .register(meterRegistry)

    private val persistFailuresCounter: Counter = Counter.builder("sse.persist.failures")
        .description("Total end-of-stream DB persistence failures")
        .register(meterRegistry)

    init {
        Gauge.builder("sse.active.connections", activeConnectionsCount) { it.get().toDouble() }
            .description("Current number of active SSE connections")
            .register(meterRegistry)
    }

    fun openConnection(): ConnectionHandle {
        activeConnectionsCount.incrementAndGet()
        return ConnectionHandle(activeConnectionsCount)
    }

    fun recordEventSent() {
        eventsSentCounter.increment()
    }

    fun recordPersistFailure() {
        persistFailuresCounter.increment()
    }

    fun activeConnections(): Int = activeConnectionsCount.get()

    class ConnectionHandle(private val counter: AtomicInteger) : AutoCloseable {
        private val closed = AtomicBoolean(false)

        override fun close() {
            if (closed.compareAndSet(false, true)) {
                counter.decrementAndGet()
            }
        }
    }
}
