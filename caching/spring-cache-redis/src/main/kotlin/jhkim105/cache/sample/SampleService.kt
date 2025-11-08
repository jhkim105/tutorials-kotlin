package jhkim105.cache.sample

import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.absoluteValue

@Service
open class SampleService {

    private val singleLookupCounter = AtomicInteger(0)
    private val listLookupCounter = AtomicInteger(0)
    private val logger = LoggerFactory.getLogger(javaClass)

    @Cacheable(cacheNames = [SINGLE_CACHE], key = "#sampleId")
    open fun getSample(sampleId: Long): SamplePayload {
        singleLookupCounter.incrementAndGet()
        val payload = buildPayload(sampleId, "category-${sampleId % 3}")
        logger.info("Loaded sample {} from source (cache miss)", sampleId)
        return payload
    }

    @Cacheable(cacheNames = [LIST_CACHE], key = "#category + ':' + #limit")
    open fun getSamples(category: String, limit: Int): List<SamplePayload> {
        listLookupCounter.incrementAndGet()
        val seed = category.hashCode().toLong().absoluteValue
        val items = (0 until limit).map { idx ->
            buildPayload(seed + idx, category)
        }
        logger.info("Loaded {} samples for category {} from source (cache miss)", items.size, category)
        return items
    }

    fun singleInvocationCount(): Int = singleLookupCounter.get()

    fun listInvocationCount(): Int = listLookupCounter.get()

    fun resetCounters() {
        singleLookupCounter.set(0)
        listLookupCounter.set(0)
    }

    private fun buildPayload(id: Long, category: String): SamplePayload {
        return SamplePayload(
            id = id,
            category = category,
            title = "Sample-$id",
            description = "Generated payload for $category $id",
            createdAt = Instant.now(),
            tags = listOf(category, "generated"),
        )
    }

    companion object {
        const val SINGLE_CACHE = "sampleSingle"
        const val LIST_CACHE = "sampleList"
    }
}
