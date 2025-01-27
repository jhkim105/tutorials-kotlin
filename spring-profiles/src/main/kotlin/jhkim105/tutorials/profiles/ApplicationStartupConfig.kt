package jhkim105.tutorials.profiles

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ApplicationStartupConfig {

    private val log: Logger = LoggerFactory.getLogger(ApplicationStartupConfig::class.java)
    @Bean
    fun logServiceProperties(serviceProperties: ServiceProperties): ApplicationRunner {
        return ApplicationRunner {
            log.info("ServiceProperties=$serviceProperties")
        }
    }
}