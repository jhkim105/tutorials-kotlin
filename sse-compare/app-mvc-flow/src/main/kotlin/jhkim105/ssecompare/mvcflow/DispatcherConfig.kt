package jhkim105.ssecompare.mvcflow

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private val logger = KotlinLogging.logger {}

@Configuration
class DispatcherConfig(
    @Value("\${sse.compare.mvc-flow.dispatcher:default}") private val dispatcherType: String,
) {
    private var vtExecutor: ExecutorService? = null

    @Bean("sseFlowDispatcher", destroyMethod = "")
    fun sseFlowDispatcher(): CoroutineDispatcher {
        return if (dispatcherType.equals("vt", ignoreCase = true)) {
            logger.info { "Using Virtual Thread CoroutineDispatcher for MVC Flow (Config C2)" }
            val exec = Executors.newVirtualThreadPerTaskExecutor()
            vtExecutor = exec
            exec.asCoroutineDispatcher()
        } else {
            logger.info { "Using Dispatchers.Default for MVC Flow (Config C1)" }
            Dispatchers.Default
        }
    }

    @PreDestroy
    fun shutdown() {
        vtExecutor?.shutdownNow()
    }
}
