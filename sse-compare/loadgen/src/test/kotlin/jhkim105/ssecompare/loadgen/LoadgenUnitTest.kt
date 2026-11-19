package jhkim105.ssecompare.loadgen

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.doubles.shouldBeBetween
import io.kotest.matchers.shouldBe
import jhkim105.ssecompare.loadgen.report.Aggregator

class LoadgenUnitTest : BehaviorSpec({
    given("MetricsRecorder") {
        `when`("여러 스트림 결과를 기록하면") {
            then("백분위수(p50/p95/p99), 에러율, 처리량을 정확히 계산한다") {
                val recorder = MetricsRecorder()
                for (i in 1..100) {
                    recorder.record(
                        StreamOutcome(
                            success = i <= 98,
                            ttfbMs = i.toLong(),
                            eventGapsMs = listOf(100L + i),
                            eventsReceived = if (i <= 98) 10 else 0,
                        ),
                    )
                }

                val snap = recorder.snapshot(durationMs = 2000L)
                snap.totalStreams shouldBe 100
                snap.completedStreams shouldBe 98
                snap.failedStreams shouldBe 2
                snap.errorRate.shouldBeBetween(0.019, 0.021, 0.0001)
                snap.ttfbMs.p50 shouldBe 50.0
                snap.ttfbMs.p95 shouldBe 95.0
                snap.ttfbMs.p99 shouldBe 99.0
                snap.eventGapMs.p99 shouldBe 199.0
                snap.throughputEventsPerSec shouldBe 490.0
            }
        }
    }

    given("LimitDetector") {
        val base = ClientMetricsSnapshot(
            ttfbMs = Percentiles(10.0, 20.0, 30.0, 12.0, 40.0),
            eventGapMs = Percentiles(100.0, 110.0, 150.0, 105.0, 160.0),
            errorRate = 0.005,
            throughputEventsPerSec = 500.0,
            throughputStreamsPerSec = 50.0,
            completionRate = 0.995,
            totalStreams = 200,
            completedStreams = 199,
            failedStreams = 1,
            totalEvents = 1990,
            durationMs = 4000,
        )

        `when`("에러율 <= 1% 이고 gap p99 <= 2 * intervalMs 일 때") {
            then("한계에 도달하지 않는다") {
                LimitDetector.evaluate(base, intervalMs = 100).reached.shouldBeFalse()
            }
        }

        `when`("에러율이 1%를 초과하면") {
            then("한계 도달로 판정한다") {
                val res = LimitDetector.evaluate(base.copy(errorRate = 0.02), intervalMs = 100)
                res.reached.shouldBeTrue()
            }
        }

        `when`("eventGap p99가 intervalMs 의 2배를 초과하면") {
            then("한계 도달로 판정한다") {
                val res = LimitDetector.evaluate(
                    base.copy(eventGapMs = base.eventGapMs.copy(p99 = 201.0)),
                    intervalMs = 100,
                )
                res.reached.shouldBeTrue()
            }
        }
    }

    given("Aggregator 편차 계산 (SC-004)") {
        `when`("3회 반복 값의 편차를 계산하면") {
            then("중앙값과 최대 편차율을 반환한다") {
                val med = Aggregator.median(listOf(100.0, 105.0, 130.0))
                med shouldBe 105.0
                val dev = Aggregator.maxDeviationPct(listOf(100.0, 105.0, 130.0), med)
                dev.shouldBeBetween(23.0, 24.5, 0.1)
            }
        }
    }
})
