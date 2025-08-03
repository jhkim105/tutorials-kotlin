package jhkim105.tutorials.springresilience4j.retry

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.resilience4j.core.registry.EntryAddedEvent
import io.github.resilience4j.core.registry.EntryRemovedEvent
import io.github.resilience4j.core.registry.EntryReplacedEvent
import io.github.resilience4j.core.registry.RegistryEventConsumer
import io.github.resilience4j.retry.Retry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RetryEventLoggingConfiguration {
    private val log = KotlinLogging.logger {}

    @Bean
    fun registryEventConsumer(): RegistryEventConsumer<Retry> =
        object : RegistryEventConsumer<Retry> {

            override fun onEntryAddedEvent(entryAddedEvent: EntryAddedEvent<Retry>) {
                val retry = entryAddedEvent.addedEntry

                retry.eventPublisher
                    .onRetry { event ->
                        log.info {
                            """
                            | [RETRY] ${event.name}: Attempt ${event.numberOfRetryAttempts}
                            | Reason: ${event.lastThrowable?.message ?: "Unknown"}
                            """.trimMargin()
                        }
                    }
                    .onError { event ->
                        log.error {
                            """
                            | [ERROR] ${event.name}: Retry exhausted
                            | Reason: ${event.lastThrowable?.message ?: "Unknown"}
                            """.trimMargin()
                        }
                    }
                    .onEvent { event ->
                        log.debug { "[EVENT] ${event.name}: $event" }
                    }
            }

            override fun onEntryRemovedEvent(entryRemoveEvent: EntryRemovedEvent<Retry>) {
                log.debug { "Retry removed: ${entryRemoveEvent.removedEntry.name}" }
            }

            override fun onEntryReplacedEvent(entryReplacedEvent: EntryReplacedEvent<Retry>) {
                log.debug {
                    "Retry replaced: ${entryReplacedEvent.oldEntry.name} → ${entryReplacedEvent.newEntry.name}"
                }
            }
        }
}
