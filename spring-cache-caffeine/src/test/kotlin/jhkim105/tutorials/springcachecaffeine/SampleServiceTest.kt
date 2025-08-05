package jhkim105.tutorials.springcachecaffeine

import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.Test

@SpringBootTest
class SampleServiceTest @Autowired constructor(
    private val sampleService: SampleService,
) {


    @Test
    fun cacheTest() {
        val data = mutableSetOf<String>()
        val format = "mm:ss SSS"

        data.add(sampleService.getCache(format))
        data.add(sampleService.getCache(format))

        sleep(10)
        sampleService.evictCache(format)

        data.add(sampleService.getCache(format))
        data.add(sampleService.getCache(format))
        data.add(sampleService.getCache(format))

        sleep(10)
        sampleService.evictSingleCacheByCacheManager(format)

        data.add(sampleService.getCache(format))

        assertThat(data).hasSize(3)
    }

    private fun sleep(millis: Long) {
        try {
            Thread.sleep(millis)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
}