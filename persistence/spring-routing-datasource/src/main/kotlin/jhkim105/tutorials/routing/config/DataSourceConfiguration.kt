package jhkim105.tutorials.routing.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy
import javax.sql.DataSource

private val logger = KotlinLogging.logger {}

@Configuration
@EnableConfigurationProperties(DataSourceProperties::class)
class DataSourceConfiguration(
    private val properties: DataSourceProperties,
) {

    @Bean(name = ["masterDataSource"])
    fun masterDataSource(): HikariDataSource {
        logger.info { "Configuring Master DataSource: ${properties.master.poolName}" }
        return createHikariDataSource(properties.master.copy(poolName = properties.master.poolName.ifBlank { "HikariPool-Master" }))
    }

    @Bean(name = ["slaveDataSources"])
    fun slaveDataSources(): Map<String, HikariDataSource> {
        return properties.slaves.mapValues { (key, slaveProp) ->
            logger.info { "Configuring Slave DataSource [$key]: ${slaveProp.poolName}" }
            createHikariDataSource(slaveProp.copy(poolName = slaveProp.poolName.ifBlank { "HikariPool-$key" }))
        }
    }

    @Bean
    fun routingDataSource(
        masterDataSource: HikariDataSource,
        slaveDataSources: Map<String, HikariDataSource>,
    ): DataSource {
        val targetDataSources = mutableMapOf<Any, Any>()
        targetDataSources[DataSourceType.MASTER] = masterDataSource

        slaveDataSources.forEach { (key, dataSource) ->
            targetDataSources[key] = dataSource
        }

        val slaveKeys = slaveDataSources.keys.toList().ifEmpty { listOf(DataSourceType.MASTER.name) }
        val slaveLoadBalancer = SlaveLoadBalancer(
            if (slaveDataSources.isEmpty()) listOf(DataSourceType.MASTER.name) else slaveKeys
        )

        val routingDataSource = RoutingDataSource(slaveLoadBalancer)
        routingDataSource.setTargetDataSources(targetDataSources)
        routingDataSource.setDefaultTargetDataSource(masterDataSource)
        routingDataSource.afterPropertiesSet()
        return routingDataSource
    }

    @Primary
    @Bean
    fun dataSource(routingDataSource: DataSource): DataSource {
        return LazyConnectionDataSourceProxy(routingDataSource)
    }

    private fun createHikariDataSource(prop: DataSourcePoolProperties): HikariDataSource {
        val config = HikariConfig().apply {
            jdbcUrl = prop.url
            username = prop.username
            password = prop.password
            driverClassName = prop.driverClassName
            poolName = prop.poolName
            maximumPoolSize = prop.maximumPoolSize
            minimumIdle = prop.minimumIdle
            connectionTimeout = prop.connectionTimeout
            idleTimeout = prop.idleTimeout
            maxLifetime = prop.maxLifetime
            isRegisterMbeans = true
        }
        return HikariDataSource(config)
    }
}
