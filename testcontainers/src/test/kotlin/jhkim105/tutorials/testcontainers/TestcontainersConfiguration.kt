package jhkim105.tutorials.testcontainers

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import java.time.Duration

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    fun mysqlContainer(): MySQLContainer<*> {
        return MySQLContainer("mysql:8.0.33").apply {
            withDatabaseName("test")
            withUsername("test")
            withPassword("test")
            withExposedPorts(3306)
            withStartupTimeout(Duration.ofSeconds(60)) // 충분한 대기 시간 확보
            waitingFor(Wait.forLogMessage(".*ready for connections.*\\s", 1)) // 로그 기준
        }
    }

}
